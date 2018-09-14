package uk.gov.companieshouse.document.generator.consumer.document.service;

import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

import java.io.IOException;
import java.text.DateFormat;
import java.util.concurrent.ExecutionException;

public interface MessageService {

    /**
     * Create message for producer when document generation has started
     *
     * @param renderSubmittedDataDocument
     * @param producer
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    Message createDocumentGenerationStarted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                            CHKafkaProducer producer) throws IOException, ExecutionException, InterruptedException;

    /**
     * Create message for producer when document generation has failed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @param producer
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    Message createDocumentGenerationFailed(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                        GenerateDocumentResponse response,
                                        CHKafkaProducer producer) throws IOException, ExecutionException, InterruptedException;

    /**
     * Create message for producer when document generation has completed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @param isoDateFormat
     * @param producer
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    Message createDocumentGenerationCompleted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                           GenerateDocumentResponse response,
                                           DateFormat isoDateFormat,
                                           CHKafkaProducer producer) throws IOException, ExecutionException, InterruptedException;
}
