package uk.gov.companieshouse.document.generator.consumer.processor.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
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

    private static final Logger LOG = LoggerFactory.getLogger("document-generator-consumer");

    private static final String RESOURCE_URI = "resource_uri";
    private static final String RESOURCE_ID = "resource_id";
    private static final String KAFKA_MSG = "kafka_message";

    private static final String CONSUMER_TOPIC_VAR = "CONSUMER_TOPIC";
    private static final String GROUP_NAME_VAR = "GROUP_NAME";

    @Autowired
    private MessageService messageService;

    @Autowired
    private GenerateDocument generateDocument;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer;

    /**
     * {inheritDocs}
     */
    @Override
    public void processKafkaMessage()
            throws InterruptedException, ExecutionException {

        LOG.info("AWAITING CONSUMATION");

        List<Message> kafkaMessages = kafkaConsumerService.consume();

        if (kafkaMessages.isEmpty()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                LOG.debug("Interrupt exception - exiting message processing");
                return;
            }
        } else {
            LOG.debug("Consumed messages " + kafkaMessages);
        }

        DeserialisedKafkaMessage deserialisedKafkaMessage = null;

        for (Message message : kafkaMessages) {

            try {
                deserialisedKafkaMessage = avroDeserializer.deserialize(message, DeserialisedKafkaMessage.getClassSchema());
            } catch (IOException ioe) {
                LOG.error("An error occurred when trying to generate a document from a kafka message", ioe,
                        setDebugMapKafkaFail(kafkaMessages.toString()));
                try {
                    kafkaProducerService.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
                } catch (MessageCreationException mce) {
                    kafkaConsumerService.commit();
                }
            }

            LOG.infoContext(deserialisedKafkaMessage.getUserId(), "Message received and deserialised from kafka",
                    setDebugMap(deserialisedKafkaMessage));

            try {
                kafkaProducerService.send(messageService.createDocumentGenerationStarted(deserialisedKafkaMessage));
            } catch (MessageCreationException mce) {
                LOG.error(mce);
            }
            requestGenerateDocument(deserialisedKafkaMessage);

            kafkaConsumerService.commit();
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
    private void requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage)
            throws ExecutionException, InterruptedException {

        try {
            ResponseEntity<GenerateDocumentResponse> response = generateDocument.requestGenerateDocument(deserialisedKafkaMessage);

            try {
                kafkaProducerService.send(messageService.createDocumentGenerationCompleted(deserialisedKafkaMessage, response.getBody()));
            } catch (MessageCreationException mce) {
                LOG.error(mce);
            }

        } catch (GenerateDocumentException gde) {
            LOG.errorContext(deserialisedKafkaMessage.getUserId(),"An error occurred when requesting the generation" +
                    " of a document from the document generator api", gde, setDebugMap(deserialisedKafkaMessage));
            try {
                kafkaProducerService.send(messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null));
            } catch (MessageCreationException mce) {
                LOG.error(mce);
            }
            kafkaConsumerService.commit();
        }
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
}
