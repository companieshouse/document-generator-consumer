package uk.gov.companieshouse.document.generator.consumer.kafka;

import java.util.List;
import java.util.Optional;
import uk.gov.companieshouse.kafka.message.Message;

public interface KafkaConsumerService {

    void connect();

    List<Message> consume();

    void commit(Message message);

    void closeConsumer();

    // Required for testing purposes
    Optional<Message> getLastMessage();

}
