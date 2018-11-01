package uk.gov.companieshouse.document.generator.consumer.kafka;

import uk.gov.companieshouse.kafka.message.Message;

import java.util.concurrent.ExecutionException;

public interface KafkaProducerService {

    void send(Message kafkaMessage) throws ExecutionException, InterruptedException;
}
