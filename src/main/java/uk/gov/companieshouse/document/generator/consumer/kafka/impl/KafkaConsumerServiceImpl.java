package uk.gov.companieshouse.document.generator.consumer.kafka.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
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

    private CHKafkaConsumerGroup consumer;

    private static final Logger LOG = LoggerFactory.getLogger("document-generator-consumer");

    public KafkaConsumerServiceImpl() {

        LOG.debug("Creating kafka consumer service " + this.toString());

        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setTopics(Arrays.asList("render-submitted-data-document"));
        consumerConfig.setGroupName("document-generator");
        consumerConfig.setResetOffset(false);

        ConsumerConfigHelper.assignBrokerAddresses(consumerConfig);
        consumer = new CHKafkaConsumerGroup(consumerConfig);
    }

    @Override
    public void connect() {
        LOG.debug("Connecting to kafka consumer service " + this.toString());
        consumer.connect();
    }

    @Override
    public List<Message> consume() {
        return consumer.consume();
    }

    @Override
    public void commit() {
        consumer.commit();
    }

    @Override
    public void closeConsumer() {
        LOG.debug("Closing kafka consumer service " + this.toString());
        consumer.close();
    }
}
