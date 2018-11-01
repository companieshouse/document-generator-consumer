package uk.gov.companieshouse.document.generator.consumer.processor;

public interface MessageProcessor {

    /**
     * process the messages from Kafka and generate a document
     *
     * @throws InterruptedException
     */
    void processKafkaMessage() throws InterruptedException;
}
