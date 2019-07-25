package uk.gov.companieshouse.document.generator.consumer.document.service;

import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.kafka.message.Message;

public interface MessageService {

    /**
     * Create message for producer when document generation has started
     *
     * @param renderSubmittedDataDocument
     * @throws MessageCreationException
     * @return
     */
    Message createDocumentGenerationStarted(RenderSubmittedDataDocument renderSubmittedDataDocument) throws MessageCreationException;

    /**
     * Create message for producer when document generation has failed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @throws MessageCreationException
     * @return
     */
    Message createDocumentGenerationFailed(RenderSubmittedDataDocument renderSubmittedDataDocument,
                                        GenerateDocumentResponse response) throws MessageCreationException;

    /**
     * Create message for producer when document generation has completed
     *
     * @param renderSubmittedDataDocument
     * @param response
     * @throws MessageCreationException
     * @return
     */
    Message createDocumentGenerationCompleted(RenderSubmittedDataDocument renderSubmittedDataDocument,
                                           GenerateDocumentResponse response) throws MessageCreationException;
}
