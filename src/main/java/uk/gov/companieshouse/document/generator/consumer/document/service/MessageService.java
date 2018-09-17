package uk.gov.companieshouse.document.generator.consumer.document.service;

import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.text.DateFormat;
import java.util.concurrent.ExecutionException;

public interface MessageService {

    /**
     * Create message for producer when document generation has started
     *
     * @param renderSubmittedDataDocument
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    Message createDocumentGenerationStarted(DeserialisedKafkaMessage renderSubmittedDataDocument) throws IOException;

    /**
     * Create message for producer when document generation has failed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    Message createDocumentGenerationFailed(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                        GenerateDocumentResponse response) throws IOException;

    /**
     * Create message for producer when document generation has completed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @param isoDateFormat
     * @throws IOException
     * @throws ExecutionException
     * @throws InterruptedException
     * @return
     */
    Message createDocumentGenerationCompleted(DeserialisedKafkaMessage renderSubmittedDataDocument,
                                           GenerateDocumentResponse response,
                                           DateFormat isoDateFormat) throws IOException;
}
