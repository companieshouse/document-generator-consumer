package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

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

public class MessageServiceImpl implements MessageService {

    @Override
    public void createDocumentGenerationStarted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                                DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer,
                                                CHKafkaProducer producer,
                                                String topic) throws IOException, ExecutionException, InterruptedException {

        DocumentGenerationStarted started = new DocumentGenerationStarted();
        started.setId(renderSubmittedDataDocument.getId());
        started.setRequesterId(renderSubmittedDataDocument.getUserId());

        byte[] startedData = documentGenerationStateAvroSerializer.serialize(started);
        producer.send(createMessage(startedData, topic));
    }

    @Override
    public void createDocumentGenerationFailed(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                               GenerateDocumentResponse response,
                                               DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer,
                                               CHKafkaProducer producer,
                                               String topic) throws IOException, ExecutionException, InterruptedException {

        DocumentGenerationFailed failed = new DocumentGenerationFailed();
        failed.setId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getId() : "");
        failed.setRequesterId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getUserId() : "");
        failed.setDescription(response != null ? response.getDescription() : "");
        failed.setDescriptionIdentifier(response != null ? response.getDescriptionIdentifier() : "");

        if (response != null && response.getDescriptionValues() != null) {
            failed.setDescriptionValues(response.getDescriptionValues());
        }

        byte[] failedData = documentGenerationStateAvroSerializer.serialize(failed);
        producer.send(createMessage(failedData, topic));
    }

    @Override
    public void createDocumentGenerationCompleted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                                  GenerateDocumentResponse response,
                                                  DateFormat isoDateFormat,
                                                  DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer,
                                                  CHKafkaProducer producer,
                                                  String topic) throws IOException, ExecutionException, InterruptedException {

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
        producer.send(createMessage(completedData, topic));
    }

    private Message createMessage(byte[] data, String topic) {
        Message message = new Message();
        message.setValue(data);
        message.setTopic(topic);
        message.setTimestamp(new Date().getTime());
        return message;
    }
}
