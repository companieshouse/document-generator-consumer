package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.avro.DocumentGenerationStateAvroSerializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DocumentGenerationStarted;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;

@Service
public class MessageServiceImpl implements MessageService {

    private static final String STARTED_PRODUCER_TOPIC = "document-generation-started";
    private static final String FAILED_PRODUCER_TOPIC = "document-generation-failed";
    private static final String COMPLETED_PRODUCER_TOPIC = "document-generation-completed";

    private DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer = new DocumentGenerationStateAvroSerializer();

    @Override
    public Message createDocumentGenerationStarted(DeserialisedKafkaMessage renderSubmittedDataDocument) throws IOException {

        DocumentGenerationStarted started = new DocumentGenerationStarted();
        started.setId(renderSubmittedDataDocument.getId());
        started.setRequesterId(renderSubmittedDataDocument.getUserId());

        byte[] startedData = documentGenerationStateAvroSerializer.serialize(started);
        return createMessage(startedData, STARTED_PRODUCER_TOPIC);
    }

    @Override
    public Message createDocumentGenerationFailed(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                               GenerateDocumentResponse response) throws IOException {

        DocumentGenerationFailed failed = new DocumentGenerationFailed();
        failed.setId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getId() : "");
        failed.setRequesterId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getUserId() : "");
        failed.setDescription(response != null ? response.getDescription() : "");
        failed.setDescriptionIdentifier(response != null ? response.getDescriptionIdentifier() : "");

        if (response != null && response.getDescriptionValues() != null) {
            failed.setDescriptionValues(response.getDescriptionValues());
        }

        byte[] failedData = documentGenerationStateAvroSerializer.serialize(failed);
        return createMessage(failedData, FAILED_PRODUCER_TOPIC);
    }

    @Override
    public Message createDocumentGenerationCompleted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                                  GenerateDocumentResponse response,
                                                  DateFormat isoDateFormat) throws IOException {

        DocumentGenerationCompleted completed = new DocumentGenerationCompleted();

        completed.setId(renderSubmittedDataDocument.getId());
        completed.setRequesterId(renderSubmittedDataDocument.getUserId());
        completed.setDescription(response.getDescription());
        completed.setDescriptionIdentifier(response.getDescriptionIdentifier());
        completed.setLocation(response.getLinks());
        completed.setDocumentSize(response.getSize());
        completed.setDocumentCreatedAt(isoDateFormat.format(new Date(System.currentTimeMillis())));
        completed.setDescriptionValues(response.getDescriptionValues());

        byte[] completedData = documentGenerationStateAvroSerializer.serialize(completed);
        return createMessage(completedData, COMPLETED_PRODUCER_TOPIC);
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
