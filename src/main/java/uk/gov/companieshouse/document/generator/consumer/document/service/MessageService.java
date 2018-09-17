package uk.gov.companieshouse.document.generator.consumer.document.service;

import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface MessageService {

    /**
     * Create message for producer when document generation has started
     *
     * @param deserialisedKafkaMessage
     * @throws IOException
     * @return
     */
    Message createDocumentGenerationStarted(DeserialisedKafkaMessage deserialisedKafkaMessage) throws IOException;

    /**
     * Create message for producer when document generation has failed
     *
     * @param deserialisedKafkaMessage
     * @param response
     * @throws IOException
     * @return
     */
    Message createDocumentGenerationFailed(DeserialisedKafkaMessage deserialisedKafkaMessage,
                                        GenerateDocumentResponse response) throws IOException;

    /**
     * Create message for producer when document generation has completed
     *
     * @param deserialisedKafkaMessage
     * @param response
     * @throws IOException
     * @return
     */
    Message createDocumentGenerationCompleted(DeserialisedKafkaMessage deserialisedKafkaMessage,
                                           GenerateDocumentResponse response) throws IOException;
}
