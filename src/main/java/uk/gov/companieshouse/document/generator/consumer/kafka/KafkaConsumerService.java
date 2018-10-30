package uk.gov.companieshouse.document.generator.consumer.kafka;

import uk.gov.companieshouse.kafka.message.Message;

import java.util.List;

public interface KafkaConsumerService {

    void connect();

    List<Message> consume();

    void commit();

    void closeConsumer();
}
