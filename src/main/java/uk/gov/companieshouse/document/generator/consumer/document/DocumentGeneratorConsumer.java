package uk.gov.companieshouse.document.generator.consumer.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerProperties;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentRequest;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerProducerHandler;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DocumentGeneratorConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger("document-generator-consumer");

    private static final String CONSUMER_TOPIC_VAR = "CONSUMER_TOPIC";
    private static final String GROUP_NAME_VAR = "GROUP_NAME";
    private static final String RESOURCE_URI = "resource_uri";
    private static final String RESOURCE_ID = "resource_id";
    private static final String KAFKA_MSG = "kafka_message";

    private boolean active = true;
    private boolean processing = false;

    private MessageService messageService;

    private CHKafkaConsumerGroup consumerGroup;

    private CHKafkaProducer producer;

    private KafkaConsumerProducerHandler kafkaConsumerProducerHandler;

    private EnvironmentReader environmentReader;

    private AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer;

    private DocumentGeneratorConsumerProperties configuration;

    @Autowired
    public DocumentGeneratorConsumer(KafkaConsumerProducerHandler kafkaConsumerProducerHandler,
                                     EnvironmentReader environmentReader,
                                     MessageService messageService,
                                     AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer,
                                     DocumentGeneratorConsumerProperties configuration) {

        this.kafkaConsumerProducerHandler = kafkaConsumerProducerHandler;
        this.environmentReader = environmentReader;
        this.messageService = messageService;
        this.avroDeserializer = avroDeserializer;
        this.configuration = configuration;

        consumerGroup = kafkaConsumerProducerHandler.getConsumerGroup(Arrays.asList(
                environmentReader.getMandatoryString(CONSUMER_TOPIC_VAR)),
                environmentReader.getMandatoryString(GROUP_NAME_VAR));

        producer = kafkaConsumerProducerHandler.getProducer();
    }

    @Override
    public void run() {
        try {
            while(isActive()) {
                setProcessing(true);
                pollAndGenerateDocument();
            }
        } catch(Exception e) {
            LOG.error(e);
        } finally {
            setProcessing(false);
        }
    }

    /**
     * Poll kafka messages to request document generation from document-generator-api
     *
     * @throws MessageCreationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Scheduled(fixedDelay = 5000)
    public void pollAndGenerateDocument() throws MessageCreationException, ExecutionException, InterruptedException {
        LOG.info("AWAITING CONSUMATION");

        List<Message> kafkaMessages = consumerGroup.consume();

        if (kafkaMessages.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                LOG.debug("Interrupt exception - exiting message processing");
                return;
            }
        } else {
            LOG.debug("Consumed messages " + kafkaMessages);
        }

        DeserialisedKafkaMessage deserialisedKafkaMessage = null;

        try {
            for (Message message : kafkaMessages) {

                deserialisedKafkaMessage = avroDeserializer.deserialize(message, DeserialisedKafkaMessage.getClassSchema());

                LOG.infoContext(deserialisedKafkaMessage.getUserId(), "Message received and deserialised from kafka",
                        setDebugMap(deserialisedKafkaMessage));

                producer.send(messageService.createDocumentGenerationStarted(deserialisedKafkaMessage));

                requestGenerateDocument(deserialisedKafkaMessage);

                consumerGroup.commit();
            }
        } catch (IOException ioe) {
            LOG.error("An error occurred when trying to generate a document from a kafka message", ioe,
                    setDebugMapKafkaFail(kafkaMessages.toString()));
            producer.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
            consumerGroup.commit();
        }
    }

    /**
     * Request generation of document from document-generator-api
     *
     * @param deserialisedKafkaMessage
     * @throws MessageCreationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage)
            throws MessageCreationException, ExecutionException, InterruptedException {
            String url = configuration.getRootUri() + configuration.getBaseUrl();

            GenerateDocumentRequest request = populateDocumentRequest(deserialisedKafkaMessage);

            LOG.infoContext(deserialisedKafkaMessage.getUserId(), "Sending request to generate document to document" +
                            " generator api", setDebugMap(deserialisedKafkaMessage));

        try {
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<GenerateDocumentResponse> response = restTemplate.postForEntity(url, request, GenerateDocumentResponse.class);

            producer.send(messageService.createDocumentGenerationCompleted(deserialisedKafkaMessage, response.getBody()));
        } catch (Exception e) {
            LOG.errorContext(deserialisedKafkaMessage.getUserId(),"An error occurred when requesting the generation" +
                    " of a document from the document generator api", e, setDebugMap(deserialisedKafkaMessage));
            producer.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
            consumerGroup.commit();
        }
    }

    /**
     * Populate GenerateDocumentRequest with DeserialisedKafkaMessage content
     *
     * @param deserialisedKafkaMessage
     * @return message
     */
    private GenerateDocumentRequest populateDocumentRequest(DeserialisedKafkaMessage deserialisedKafkaMessage) {
        GenerateDocumentRequest request = new GenerateDocumentRequest();
        request.setResourceUri(deserialisedKafkaMessage.getResourceId());
        request.setResourceID(deserialisedKafkaMessage.getResource());
        request.setMimeType(deserialisedKafkaMessage.getContentType());
        request.setDocumentType(deserialisedKafkaMessage.getDocumentType());
        return request;
    }

    private Map<String, Object> setDebugMap(DeserialisedKafkaMessage deserialisedKafkaMessage) {

        Map<String, Object> debugMap = new HashMap<>();
        debugMap.put(RESOURCE_URI, deserialisedKafkaMessage.getResource());
        debugMap.put(RESOURCE_ID, deserialisedKafkaMessage.getResourceId());

        return debugMap;
    }

    private Map<String, Object> setDebugMapKafkaFail(String kafkaMessage) {

        Map<String, Object> kafkaFailDebugMap = new HashMap<>();
        kafkaFailDebugMap.put(KAFKA_MSG, kafkaMessage);

        return kafkaFailDebugMap;
    }

    /**
     * Set is processing flag
     *
     * @param processing
     */
    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    /**
     * Is message processor still active
     *
     * @return boolean
     */
    public boolean isActive() {
        return active;
    }
}