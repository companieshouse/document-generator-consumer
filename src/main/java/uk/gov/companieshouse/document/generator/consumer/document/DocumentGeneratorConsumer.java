package uk.gov.companieshouse.document.generator.consumer.document;

import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class DocumentGeneratorConsumer implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger("document-generator-consumer");

    private static final String CONSUMER_TOPIC_VAR = "CONSUMER_TOPIC";
    private static final String GROUP_NAME_VAR = "GROUP_NAME";

    private MessageService messageService;

    private RestTemplate restTemplate;

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
                                     RestTemplate restTemplate,
                                     CHKafkaProducer producer,
                                     DocumentGeneratorConsumerProperties configuration) {

        this.kafkaConsumerProducerHandler = kafkaConsumerProducerHandler;
        this.environmentReader = environmentReader;
        this.messageService = messageService;
        this.avroDeserializer = avroDeserializer;
        this.restTemplate = restTemplate;
        this.producer = producer;
        this.configuration = configuration;

        consumerGroup = kafkaConsumerProducerHandler.getConsumerGroup(Arrays.asList(
                environmentReader.getMandatoryString(CONSUMER_TOPIC_VAR)),
                environmentReader.getMandatoryString(GROUP_NAME_VAR));
    }

    @Override
    public void run() {
        try {
            pollAndGenerateDocument();
        } catch(Exception e) {
            LOG.error(e);
        }
    }

    /**
     * Poll kafka messages to request document generation from document-generator-api
     *
     * @throws MessageCreationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public void pollAndGenerateDocument() throws MessageCreationException, ExecutionException, InterruptedException {
        for (Message message : consumerGroup.consume()) {
            DeserialisedKafkaMessage deserialisedKafkaMessage = null;

            try {
                deserialisedKafkaMessage = avroDeserializer.deserialize(message, DeserialisedKafkaMessage.getClassSchema());

                producer.send(messageService.createDocumentGenerationStarted(deserialisedKafkaMessage));

                requestGenerateDocument(deserialisedKafkaMessage);

                consumerGroup.commit();
            } catch (Exception e) {
                LOG.error(e);
                producer.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
                consumerGroup.commit();
            }
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
    public void requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage) throws MessageCreationException, ExecutionException, InterruptedException {
        try {
            String url = configuration.getRootUri() + configuration.getBaseUrl();
            GenerateDocumentRequest request = populateDocumentRequest(deserialisedKafkaMessage);
            GenerateDocumentResponse response;

            response = restTemplate.postForObject(url, request, GenerateDocumentResponse.class);

            producer.send(messageService.createDocumentGenerationCompleted(deserialisedKafkaMessage, response));
        } catch (Exception e) {
            LOG.error(e);
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
        request.setResourceUri(deserialisedKafkaMessage.getResource());
        request.setResourceID(deserialisedKafkaMessage.getResourceId());
        request.setMimeType(deserialisedKafkaMessage.getContentType());
        request.setDocumentType(deserialisedKafkaMessage.getDocumentType());
        return request;
    }
}
