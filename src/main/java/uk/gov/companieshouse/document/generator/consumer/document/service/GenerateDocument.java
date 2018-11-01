package uk.gov.companieshouse.document.generator.consumer.document.service;

import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.exception.GenerateDocumentException;

public interface GenerateDocument {

    /**
     * Populates GenerateDocumentRequest object with info from the deserialised Kafka message to be sent to the document
     * generator api to generate a document.
     *
     * @param deserialisedKafkaMessage The message deserialised from Kafka
     */
    ResponseEntity requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage) throws GenerateDocumentException;
}
