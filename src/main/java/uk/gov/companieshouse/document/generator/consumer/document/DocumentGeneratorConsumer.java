package uk.gov.companieshouse.document.generator.consumer.document;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;
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

    @Autowired
    private MessageService messageService;

    @Autowired
    private RestTemplate restTemplate;

    private CHKafkaConsumerGroup consumerGroup;

    private CHKafkaProducer producer;

    private KafkaConsumerProducerHandler kafkaConsumerProducerHandler;

    private EnvironmentReader environmentReader;

    private AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer;

    private static final String DOCUMENT_GENERATE_URI = "http://localhost:8080/private/documents/generate";

    @Autowired
    public DocumentGeneratorConsumer(KafkaConsumerProducerHandler kafkaConsumerProducerHandler,
                                     EnvironmentReader environmentReader,
                                     MessageService messageService,
                                     AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer,
                                     RestTemplate restTemplate) {

        this.kafkaConsumerProducerHandler = kafkaConsumerProducerHandler;
        this.environmentReader = environmentReader;
        this.messageService = messageService;
        this.avroDeserializer = avroDeserializer;
        this.restTemplate = restTemplate;

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
     */
    public void requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage) throws MessageCreationException, ExecutionException, InterruptedException {
        GenerateDocumentRequest request = populateDocumentRequest(deserialisedKafkaMessage);
        GenerateDocumentResponse response = null;

        try {
            response = restTemplate.postForObject(DOCUMENT_GENERATE_URI, request, GenerateDocumentResponse.class);

            producer.send(messageService.createDocumentGenerationCompleted(deserialisedKafkaMessage, response));
        } catch (Exception e) {
            LOG.error(e);
            producer.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, response));
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
