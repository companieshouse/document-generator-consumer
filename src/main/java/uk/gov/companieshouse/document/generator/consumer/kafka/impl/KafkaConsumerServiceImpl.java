package uk.gov.companieshouse.document.generator.consumer.kafka.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfigHelper;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Service
public class KafkaConsumerServiceImpl  implements KafkaConsumerService {

    private EnvironmentReader reader;

    private CHKafkaConsumerGroup consumer;

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    public KafkaConsumerServiceImpl(EnvironmentReader reader) {
        LOG.debug("KafkaConsumerServiceImpl() constructor called.");

        this.reader = reader;

        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setTopics(Arrays.asList(reader.getMandatoryString(DocumentGeneratorConsumerApplication.CONSUMER_TOPIC)));
        consumerConfig.setGroupName(reader.getMandatoryString(DocumentGeneratorConsumerApplication.GROUP_NAME));
        consumerConfig.setResetOffset(false);

        ConsumerConfigHelper.assignBrokerAddresses(consumerConfig);

        consumer = new CHKafkaConsumerGroup(consumerConfig);
    }

    @Override
    public void connect() {
        LOG.debug("connect() method called.");

        consumer.connect();

        LOG.debug("Connected successfully.");
    }

    @Override
    public List<Message> consume() {
        LOG.debug("consume() method called.");

        return consumer.consume();
    }

    @Override
    public void commit(Message message) {
        LOG.debug("commit() method called.");

        consumer.commit(message);
    }

    @Override
    public void closeConsumer() {
        LOG.debug("closeConsumer() method called.");

        consumer.close();
    }
}
