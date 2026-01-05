package uk.gov.companieshouse.document.generator.consumer.kafka.impl;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class KafkaConsumerServiceImpl  implements KafkaConsumerService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private final CHKafkaConsumerGroup consumer;

    private Consumer<Message> callback;;

    public KafkaConsumerServiceImpl(final ConsumerConfig consumerConfig) {
        LOG.debug("KafkaConsumerServiceImpl() constructor called.");

        consumer = new CHKafkaConsumerGroup(consumerConfig);;
    }

    @Override
    public void connect() {
        LOG.debug("connect() method called.");

        consumer.connect();
    }

    @Override
    public List<Message> consume() {
        // Find list of messages that have been consumed from kafka topic.
        List<Message> messages = consumer.consume();

        // Process each message and log output details for debugging.
        AtomicInteger count = new AtomicInteger();

        messages.forEach(message -> {
            LOG.debug("> Consumed Message -> %d: (%d bytes received...)".formatted(count.incrementAndGet(), message.getValue().length));
        });

        return messages;
    }

    @Override
    public void commit(final Message message) {
        LOG.debug("commit(key=%s) method called.".formatted(message.getKey()));

        consumer.commit(message);

        if (callback != null) {
            callback.accept(message);
        }

        //lastMessage = Optional.of(message);
    }

    @Override
    public void closeConsumer() {
        LOG.debug("closeConsumer() method called.");

        consumer.close();
    }

    public void setCallback(final Consumer<Message> callback) {
        LOG.debug("setCallback() method called.");

        this.callback = callback;
    }

    /*
    public Optional<Message> getLastMessage() {
        LOG.debug("getLastMessage(message=%s) method called.".formatted(lastMessage));

        return lastMessage;
    }
    */
}
