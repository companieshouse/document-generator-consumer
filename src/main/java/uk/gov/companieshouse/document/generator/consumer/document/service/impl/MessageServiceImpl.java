package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.document.generation.request.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationStarted;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.document.generator.consumer.transformers.DocumentGenerationTransformer;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

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

    @Autowired
    private SerializerFactory serializerFactory;

    @Autowired
    private DocumentGenerationTransformer transformer;

    @Override
    public Message createDocumentGenerationStarted(RenderSubmittedDataDocument renderSubmittedDataDocument) throws MessageCreationException {

        DocumentGenerationStarted started = transformer.transformGenerationStarted(renderSubmittedDataDocument);

        try {
            LOG.infoContext(started.getRequesterId(),"Serialize document generation started and create message", setStartedDebugMap(started));
            AvroSerializer<DocumentGenerationStarted> serializer = serializerFactory.getSpecificRecordSerializer(DocumentGenerationStarted.class);
            byte[] bytes = serializer.toBinary(started);
  
            return createMessage(bytes, STARTED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.errorContext(started.getRequesterId(), "Error occurred whilst serialising document generation started",
                    e, setStartedDebugMap(started));
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Message createDocumentGenerationFailed(RenderSubmittedDataDocument renderSubmittedDataDocument,
                                               GenerateDocumentResponse response) throws MessageCreationException {

        DocumentGenerationFailed failed = transformer.transformGenerationFailed(renderSubmittedDataDocument, response);

        if (response != null && response.getDescriptionValues() != null) {
            failed.setDescriptionValues(response.getDescriptionValues());
        }

        try {
            LOG.infoContext(failed.getRequesterId(),"Serialize document generation failed and create message", setFailedDebugMap(failed));
            AvroSerializer<DocumentGenerationFailed> serializer = serializerFactory.getSpecificRecordSerializer(DocumentGenerationFailed.class);
            byte[] bytes = serializer.toBinary(failed);

            return createMessage(bytes, FAILED_PRODUCER_TOPIC);
        } catch (Exception e) {
            LOG.errorContext(failed.getRequesterId(),"Error occurred whilst serialising document generation failed",
                    e, setFailedDebugMap(failed));
            throw new MessageCreationException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Message createDocumentGenerationCompleted(RenderSubmittedDataDocument renderSubmittedDataDocument,
                                                  GenerateDocumentResponse response) throws MessageCreationException {

        DocumentGenerationCompleted completed = transformer.transformGenerationCompleted(renderSubmittedDataDocument, response);

        try {
            LOG.infoContext(completed.getRequesterId(),"Serialize document generation completed and create message", setCompletedDebugMap(completed));
            AvroSerializer<DocumentGenerationCompleted> serializer = serializerFactory.getSpecificRecordSerializer(DocumentGenerationCompleted.class);
            byte[] bytes = serializer.toBinary(completed);

            return createMessage(bytes, COMPLETED_PRODUCER_TOPIC);
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
        completedParams.put(COMPLETED_DOCUMENT, getObjectMapper(String.valueOf(completed)));

        return completedParams;
    }

    private Map<String, Object> setFailedDebugMap(DocumentGenerationFailed failed) {

        Map<String, Object> failedParams = new HashMap<>();
        failedParams.put(FAILED_DOCUMENT, getObjectMapper(String.valueOf(failed)));

        return failedParams;
    }

    private Map<String, Object> setStartedDebugMap(DocumentGenerationStarted started)  {
        Map<String, Object> startedParams = new HashMap<>();
        startedParams.put(STARTED_DOCUMENT, getObjectMapper(String.valueOf(started)));

        return startedParams;
    }

    private JsonNode getObjectMapper(String logStatus){
        JsonNode node = null;
       try {
           ObjectMapper mapper = new ObjectMapper();
           node= mapper.readTree(logStatus);
       }catch (JsonProcessingException ignored){

       }
       return node;
    }
}
