package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.avro.DocumentGenerationStateAvroSerializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DocumentGenerationStarted;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOG = LoggerFactory.getLogger("document-generator-consumer");

    private static final String STARTED_PRODUCER_TOPIC = "document-generation-started";
    private static final String FAILED_PRODUCER_TOPIC = "document-generation-failed";
    private static final String COMPLETED_PRODUCER_TOPIC = "document-generation-completed";

    private DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer = new DocumentGenerationStateAvroSerializer();

    @Override
    public Message createDocumentGenerationStarted(DeserialisedKafkaMessage deserialisedKafkaMessage) throws MessageCreationException {

        DocumentGenerationStarted started = new DocumentGenerationStarted();

        started.setId(deserialisedKafkaMessage.getId());
        started.setRequesterId(deserialisedKafkaMessage.getUserId());

        try {
            LOG.info("Serialize document generation started and create message ");
            byte[] startedData = documentGenerationStateAvroSerializer.serialize(started);
            return createMessage(startedData, STARTED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.error(e);
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Message createDocumentGenerationFailed(DeserialisedKafkaMessage deserialisedKafkaMessage,
                                               GenerateDocumentResponse response) throws MessageCreationException {

        DocumentGenerationFailed failed = new DocumentGenerationFailed();
        failed.setId(deserialisedKafkaMessage != null ? deserialisedKafkaMessage.getId() : "");
        failed.setRequesterId(deserialisedKafkaMessage != null ? deserialisedKafkaMessage.getUserId() : "");
        failed.setDescription(response != null ? response.getDescription() : "");
        failed.setDescriptionIdentifier(response != null ? response.getDescriptionIdentifier() : "");

        if (response != null && response.getDescriptionValues() != null) {
            failed.setDescriptionValues(response.getDescriptionValues());
        }

        try {
            LOG.info("Serialize document generation failed and create message ");
            byte[] failedData = documentGenerationStateAvroSerializer.serialize(failed);
            return createMessage(failedData, FAILED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.error(e);
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Message createDocumentGenerationCompleted(DeserialisedKafkaMessage deserialisedKafkaMessage,
                                                  GenerateDocumentResponse response) throws MessageCreationException {

        DocumentGenerationCompleted completed = new DocumentGenerationCompleted();

        completed.setId(deserialisedKafkaMessage.getId());
        completed.setRequesterId(deserialisedKafkaMessage.getUserId());
        completed.setDescription(response.getDescription());
        completed.setDescriptionIdentifier(response.getDescriptionIdentifier());
        completed.setLocation(response.getLinks().getLocation());
        completed.setDocumentSize(response.getSize());
        completed.setDocumentCreatedAt(isoDateFormat.format(new Date(System.currentTimeMillis())));
        completed.setDescriptionValues(response.getDescriptionValues());

        try {
            LOG.info("Serialize document generation completed and create message ");
            byte[] completedData = documentGenerationStateAvroSerializer.serialize(completed);
            return createMessage(completedData, COMPLETED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.error(e);
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Create message to be put onto consumer
     *
     * @param data
     * @param topic
     * @return
     */
    private Message createMessage(byte[] data, String topic) {
        Message message = new Message();
        message.setValue(data);
        message.setTopic(topic);
        message.setTimestamp(new Date().getTime());
        return message;
    }
}
