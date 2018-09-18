package uk.gov.companieshouse.document.generator.consumer.document.service;

import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.kafka.message.Message;

public interface MessageService {

    /**
     * Create message for producer when document generation has started
     *
     * @param deserialisedKafkaMessage
     * @throws MessageCreationException
     * @return
     */
    Message createDocumentGenerationStarted(DeserialisedKafkaMessage deserialisedKafkaMessage) throws MessageCreationException;

    /**
     * Create message for producer when document generation has failed
     *
     * @param deserialisedKafkaMessage
     * @param response
     * @throws MessageCreationException
     * @return
     */
    Message createDocumentGenerationFailed(DeserialisedKafkaMessage deserialisedKafkaMessage,
                                        GenerateDocumentResponse response) throws MessageCreationException;

    /**
     * Create message for producer when document generation has completed
     *
     * @param deserialisedKafkaMessage
     * @param response
     * @throws MessageCreationException
     * @return
     */
    Message createDocumentGenerationCompleted(DeserialisedKafkaMessage deserialisedKafkaMessage,
                                           GenerateDocumentResponse response) throws MessageCreationException;
}
