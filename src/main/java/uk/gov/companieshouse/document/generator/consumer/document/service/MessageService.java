package uk.gov.companieshouse.document.generator.consumer.document.service;

import uk.gov.companieshouse.document.generator.consumer.avro.DocumentGenerationStateAvroSerializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

import java.io.IOException;
import java.text.DateFormat;
import java.util.concurrent.ExecutionException;

public interface MessageService {

    /**
     * Create message for producer when document generation has started
     *
     * @param renderSubmittedDataDocument
     * @param documentGenerationStateAvroSerializer
     * @param producer
     * @param topic
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void createDocumentGenerationStarted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                         DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer,
                                         CHKafkaProducer producer,
                                         String topic) throws IOException, ExecutionException, InterruptedException;

    /**
     * Create message for producer when document generation has failed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @param documentGenerationStateAvroSerializer
     * @param producer
     * @param topic
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void createDocumentGenerationFailed(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                        GenerateDocumentResponse response,
                                        DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer,
                                        CHKafkaProducer producer,
                                        String topic) throws IOException, ExecutionException, InterruptedException;

    /**
     * Create message for producer when document generation has completed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @param isoDateFormat
     * @param documentGenerationStateAvroSerializer
     * @param producer
     * @param topic
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     */
    void createDocumentGenerationCompleted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                           GenerateDocumentResponse response,
                                           DateFormat isoDateFormat,
                                           DocumentGenerationStateAvroSerializer documentGenerationStateAvroSerializer,
                                           CHKafkaProducer producer,
                                           String topic) throws IOException, ExecutionException, InterruptedException;
}
