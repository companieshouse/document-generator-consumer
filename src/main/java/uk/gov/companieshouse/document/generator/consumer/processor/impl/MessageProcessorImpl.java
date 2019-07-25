package uk.gov.companieshouse.document.generator.consumer.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.RenderSubmittedDataDocument;
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

    private AvroDeserializer<RenderSubmittedDataDocument> avroDeserializer;

    private static final String KAFKA_MSG = "kafka_message";

    private static final String KAFKA_TOPIC = "kafka_topic";

    private static final String KAFKA_OFFSET = "kafka_offset";

    private static final String KAFKA_TIME = "kafka_time";

    @Autowired
    public MessageProcessorImpl(MessageService messageService, GenerateDocument generateDocument,
                                KafkaConsumerService kafkaConsumerService, KafkaProducerService kafkaProducerService,
                                AvroDeserializer<RenderSubmittedDataDocument> avroDeserializer) {

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

        RenderSubmittedDataDocument renderSubmittedDataDocument = null;

        for (Message message : kafkaMessages) {

            try {
                renderSubmittedDataDocument = avroDeserializer.deserialize(message, RenderSubmittedDataDocument.getClassSchema());

                LOG.infoContext(renderSubmittedDataDocument.getUserId(), "Message received and deserialised from kafka",
                        setDebugMap(renderSubmittedDataDocument, message));

                try {
                    kafkaProducerService.send(messageService.createDocumentGenerationStarted(renderSubmittedDataDocument));
                } catch (MessageCreationException | ExecutionException mce) {
                    LOG.errorContext("Error occurred while attempt to create and send a started message to producer",
                            mce, setDebugMap(renderSubmittedDataDocument, message));
                    kafkaConsumerService.commit(message);
                    continue;
                }

                requestGenerateDocument(renderSubmittedDataDocument, message);

            } catch (IOException ioe) {
                LOG.errorContext("An error occurred when trying to generate a document from a kafka message", ioe,
                        setDebugMapKafkaFail(message));
                try {
                    kafkaProducerService.send(messageService.createDocumentGenerationFailed(renderSubmittedDataDocument, null));
                    LOG.info("Document failed to generate", setDebugMapKafkaFail(message));
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
     * @param renderSubmittedDataDocument The message deserialised from Kafka
     * @throws MessageCreationException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private void requestGenerateDocument(RenderSubmittedDataDocument renderSubmittedDataDocument, Message message)
            throws InterruptedException {

        try {
            ResponseEntity<GenerateDocumentResponse> response = generateDocument.requestGenerateDocument(renderSubmittedDataDocument);

            try {
                kafkaProducerService.send(messageService.createDocumentGenerationCompleted(renderSubmittedDataDocument, response.getBody()));
                LOG.infoContext(renderSubmittedDataDocument.getUserId(),"Document has been generated for resource: "
                    + renderSubmittedDataDocument.getResource(), setDebugMap(renderSubmittedDataDocument, message));
            } catch (MessageCreationException | ExecutionException mce) {
                LOG.errorContext("Error occurred while attempt to create and send a completed message to producer",
                        mce, setDebugMap(renderSubmittedDataDocument, message));
            }

        } catch (GenerateDocumentException gde) {
            LOG.errorContext(renderSubmittedDataDocument.getUserId(),"An error occurred when requesting the generation" +
                    " of a document from the document generator api", gde, setDebugMap(renderSubmittedDataDocument, message));
            try {
                kafkaProducerService.send(messageService.createDocumentGenerationFailed(renderSubmittedDataDocument, null));
                LOG.infoContext(renderSubmittedDataDocument.getUserId(),"Document failed to generate during the " +
                    "document generator api call for resource: " + renderSubmittedDataDocument.getResource(),
                    setDebugMap(renderSubmittedDataDocument, message));
            } catch (MessageCreationException | ExecutionException mce) {
                LOG.errorContext("Error occurred while attempt to create and send a failed message message to producer",
                        mce, setDebugMap(renderSubmittedDataDocument, message));
            }
        }
    }

    private Map<String, Object> setDebugMap(RenderSubmittedDataDocument renderSubmittedDataDocument, Message message) {

        Map<String, Object> debugMap = new HashMap<>();
        debugMap.put(DocumentGeneratorConsumerApplication.RESOURCE_URI, renderSubmittedDataDocument.getResource());
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
