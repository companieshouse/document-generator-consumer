package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.avro.DocumentGenerationStateAvroSerializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.RenderSubmittedDataDocument;
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
import java.util.HashMap;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private static final String STARTED_PRODUCER_TOPIC = "document-generation-started";
    private static final String FAILED_PRODUCER_TOPIC = "document-generation-failed";
    private static final String COMPLETED_PRODUCER_TOPIC = "document-generation-completed";

    private static final String STARTED_DOCUMENT = "started_document";
    private static final String FAILED_DOCUMENT = "failed_document";
    private static final String COMPLETED_DOCUMENT = "completed_document";
    private static final String DESCRIPTION_IDENTIFIER = "description_identifier";
    private static final String DESCRIPTION = "description";

    private DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer = new DocumentGenerationStateAvroSerializer();

    @Override
    public Message createDocumentGenerationStarted(RenderSubmittedDataDocument renderSubmittedDataDocument) throws MessageCreationException {

        DocumentGenerationStarted started = new DocumentGenerationStarted();

        started.setId(renderSubmittedDataDocument.getId());
        started.setRequesterId(renderSubmittedDataDocument.getUserId());

        try {
            LOG.infoContext(started.getRequesterId(),"Serialize document generation started and create message",
                    setStartedDebugMap(started));
            byte[] startedData = documentGenerationStateAvroSerializer.serialize(started);
            return createMessage(startedData, STARTED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.errorContext(started.getRequesterId(), "Error occurred whilst serialising document generation started",
                    e, setStartedDebugMap(started));
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Message createDocumentGenerationFailed(RenderSubmittedDataDocument renderSubmittedDataDocument,
                                               GenerateDocumentResponse response) throws MessageCreationException {

        DocumentGenerationFailed failed = new DocumentGenerationFailed();
        failed.setId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getId() : "");
        failed.setRequesterId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getUserId() : "");
        failed.setDescription(response != null ? response.getDescription() : "");
        failed.setDescriptionIdentifier(response != null ? response.getDescriptionIdentifier() : "");

        if (response != null && response.getDescriptionValues() != null) {
            failed.setDescriptionValues(response.getDescriptionValues());
        }

        try {
            LOG.infoContext(failed.getRequesterId(),"Serialize document generation failed and create message",
                   setFailedDebugMap(failed));
            byte[] failedData = documentGenerationStateAvroSerializer.serialize(failed);
            return createMessage(failedData, FAILED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.errorContext(failed.getRequesterId(),"Error occurred whilst serialising document generation failed",
                    e, setFailedDebugMap(failed));
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Message createDocumentGenerationCompleted(RenderSubmittedDataDocument renderSubmittedDataDocument,
                                                  GenerateDocumentResponse response) throws MessageCreationException {

        DocumentGenerationCompleted completed = new DocumentGenerationCompleted();

        completed.setId(renderSubmittedDataDocument.getId());
        completed.setRequesterId(renderSubmittedDataDocument.getUserId());
        completed.setDescription(response.getDescription());
        completed.setDescriptionIdentifier(response.getDescriptionIdentifier());
        completed.setLocation(response.getLinks().getLocation());
        completed.setDocumentSize(response.getSize());
        completed.setDocumentCreatedAt(isoDateFormat.format(new Date(System.currentTimeMillis())));
        completed.setDescriptionValues(response.getDescriptionValues());

        try {
            LOG.infoContext(completed.getRequesterId(),"Serialize document generation completed and create message",
                    setCompletedDebugMap(completed));
            byte[] completedData = documentGenerationStateAvroSerializer.serialize(completed);
            return createMessage(completedData, COMPLETED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.errorContext(completed.getRequesterId(), "Error occurred whilst serialising document generation completed",
                    e, setCompletedDebugMap(completed));
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

    private Map<String, Object> setCompletedDebugMap(DocumentGenerationCompleted completed) {

        Map<String, Object> completedParams = new HashMap<>();
        completedParams.put(DESCRIPTION_IDENTIFIER, completed.getDescriptionIdentifier());
        completedParams.put(DESCRIPTION, completed.getDescription());
        completedParams.put(COMPLETED_DOCUMENT, completed);

        return completedParams;
    }

    private Map<String, Object> setFailedDebugMap(DocumentGenerationFailed failed) {

        Map<String, Object> failedParams = new HashMap<>();
        failedParams.put(FAILED_DOCUMENT, failed);

        return failedParams;
    }

    private Map<String, Object> setStartedDebugMap(DocumentGenerationStarted started) {

        Map<String, Object> startedParams = new HashMap<>();
        startedParams.put(STARTED_DOCUMENT, started);

        return startedParams;
    }
}
