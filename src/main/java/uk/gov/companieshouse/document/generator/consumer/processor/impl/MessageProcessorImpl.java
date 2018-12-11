package uk.gov.companieshouse.document.generator.consumer.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.GenerateDocument;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.exception.GenerateDocumentException;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaProducerService;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessor;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
public class MessageProcessorImpl implements MessageProcessor {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private MessageService messageService;

    private GenerateDocument generateDocument;

    private KafkaConsumerService kafkaConsumerService;

    private KafkaProducerService kafkaProducerService;

    private AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer;

    private static final String KAFKA_MSG = "kafka_message";

    private static final String KAFKA_TOPIC = "kafka_topic";

    private static final String KAFKA_OFFSET = "kafka_offset";

    private static final String KAFKA_TIME = "kafka_time";

    @Autowired
    public MessageProcessorImpl(MessageService messageService, GenerateDocument generateDocument,
                                KafkaConsumerService kafkaConsumerService, KafkaProducerService kafkaProducerService,
                                AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer) {

        this.messageService = messageService;
        this.generateDocument = generateDocument;
        this.kafkaConsumerService = kafkaConsumerService;
        this.kafkaProducerService = kafkaProducerService;
        this.avroDeserializer = avroDeserializer;
    }

    /**
     * {inheritDocs}
     */
    @Override
    public void processKafkaMessage() throws InterruptedException {

        List<Message> kafkaMessages = kafkaConsumerService.consume();

        if (kafkaMessages.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                LOG.debug("Interrupt exception - exiting message processing");
                Thread.currentThread().interrupt();
                return;
            }
        } else {
            LOG.debug("Consumed messages " + kafkaMessages);
        }

        DeserialisedKafkaMessage deserialisedKafkaMessage = null;

        for (Message message : kafkaMessages) {

            try {
                deserialisedKafkaMessage = avroDeserializer.deserialize(message, DeserialisedKafkaMessage.getClassSchema());

                LOG.infoContext(deserialisedKafkaMessage.getUserId(), "Message received and deserialised from kafka",
                        setDebugMap(deserialisedKafkaMessage, message));

                try {
                    kafkaProducerService.send(messageService.createDocumentGenerationStarted(deserialisedKafkaMessage));
                } catch (MessageCreationException | ExecutionException mce) {
                    LOG.errorContext("Error occurred while attempt to create and send a started message to producer",
                            mce, setDebugMap(deserialisedKafkaMessage, message));
                    kafkaConsumerService.commit(message);
                    continue;
                }

                requestGenerateDocument(deserialisedKafkaMessage, message);

            } catch (IOException ioe) {
                LOG.errorContext("An error occurred when trying to generate a document from a kafka message", ioe,
                        setDebugMapKafkaFail(message));
                try {
                    kafkaProducerService.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
                    LOG.infoContext(deserialisedKafkaMessage.getUserId(),"Document failed to generate for resource: "
                        + deserialisedKafkaMessage.getResource(), setDebugMap(deserialisedKafkaMessage, message));
                } catch (MessageCreationException |ExecutionException mce) {
                    LOG.errorContext("Error occurred while attempt to create and send a failed message to producer",
                            mce, setDebugMapKafkaFail(message));
                }
            }

            kafkaConsumerService.commit(message);
        }
    }

    /**
     * Populates GenerateDocumentRequest object with info from the deserialised Kafka message to be sent to the document
     * generator api to generate a document.
     *
     * @param deserialisedKafkaMessage The message deserialised from Kafka
     * @throws MessageCreationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage, Message message)
            throws InterruptedException {

        try {
            ResponseEntity<GenerateDocumentResponse> response = generateDocument.requestGenerateDocument(deserialisedKafkaMessage);

            try {
                kafkaProducerService.send(messageService.createDocumentGenerationCompleted(deserialisedKafkaMessage, response.getBody()));
                LOG.infoContext(deserialisedKafkaMessage.getUserId(),"Document has been generated for resource: "
                    + deserialisedKafkaMessage.getResource(), setDebugMap(deserialisedKafkaMessage, message));
            } catch (MessageCreationException | ExecutionException mce) {
                LOG.errorContext("Error occurred while attempt to create and send a completed message to producer",
                        mce, setDebugMap(deserialisedKafkaMessage, message));
            }

        } catch (GenerateDocumentException gde) {
            LOG.errorContext(deserialisedKafkaMessage.getUserId(),"An error occurred when requesting the generation" +
                    " of a document from the document generator api", gde, setDebugMap(deserialisedKafkaMessage, message));
            try {
                kafkaProducerService.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
                LOG.infoContext(deserialisedKafkaMessage.getUserId(),"Document failed to generate during the " +
                    "document generator api call for resource: " + deserialisedKafkaMessage.getResource(),
                    setDebugMap(deserialisedKafkaMessage, message));
            } catch (MessageCreationException | ExecutionException mce) {
                LOG.errorContext("Error occurred while attempt to create and send a failed message message to producer",
                        mce, setDebugMap(deserialisedKafkaMessage, message));
            }
        }
    }

    private Map<String, Object> setDebugMap(DeserialisedKafkaMessage deserialisedKafkaMessage, Message message) {

        Map<String, Object> debugMap = new HashMap<>();
        debugMap.put(DocumentGeneratorConsumerApplication.RESOURCE_URI, deserialisedKafkaMessage.getResource());
        debugMap.put(KAFKA_TOPIC, message.getTopic());
        debugMap.put(KAFKA_OFFSET, message.getOffset());
        debugMap.put(KAFKA_TIME, message.getTimestamp());

        return debugMap;
    }

    private Map<String, Object> setDebugMapKafkaFail(Message message) {

        Map<String, Object> kafkaFailDebugMap = new HashMap<>();
        kafkaFailDebugMap.put(KAFKA_MSG, message.getValue());
        kafkaFailDebugMap.put(KAFKA_TOPIC, message.getTopic());
        kafkaFailDebugMap.put(KAFKA_OFFSET, message.getOffset());
        kafkaFailDebugMap.put(KAFKA_TIME, message.getTimestamp());

        return kafkaFailDebugMap;
    }
}
