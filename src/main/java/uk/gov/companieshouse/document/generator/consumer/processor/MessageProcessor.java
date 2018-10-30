package uk.gov.companieshouse.document.generator.consumer.processor;

import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;

import java.util.concurrent.ExecutionException;

public interface MessageProcessor {

    /**
     * process the messages from Kafka and generate a document
     *
     * @throws InterruptedException
     * @throws MessageCreationException
     * @throws ExecutionException
     */
    void processKafkaMessage()
            throws InterruptedException, MessageCreationException, ExecutionException;
}
