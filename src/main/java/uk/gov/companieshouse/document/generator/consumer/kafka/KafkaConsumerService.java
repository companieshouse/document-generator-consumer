package uk.gov.companieshouse.document.generator.consumer.kafka;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import uk.gov.companieshouse.kafka.message.Message;

public interface KafkaConsumerService {

    void connect();

    List<Message> consume();

    void commit(Message message);

    void closeConsumer();

    // Required for testing purposes
    void setCallback(final Consumer<Message> callback);
    //Optional<Message> getLastMessage();

}
