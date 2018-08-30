package uk.gov.companieshouse.document.generator.consumer.kafka.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerProducerHandler;
import uk.gov.companieshouse.document.generator.consumer.kafka.configurationhelper.KafkaConfigHelper;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

import java.util.List;

@Component
public class KafkaConsumerProducerHandlerImpl implements KafkaConsumerProducerHandler {

    @Autowired
    private KafkaConfigHelper kafkaConfigHelper;

    /**
     * get the  kafka consumer group
     *
     * @param consumerTopics
     * @param groupName
     * @return
     */
    @Override
    public CHKafkaConsumerGroup getConsumerGroup(List<String> consumerTopics, String groupName) {
        return new CHKafkaConsumerGroup(kafkaConfigHelper
                .configureKafkaConsumer(consumerTopics, groupName));
    }

    /**
     * Get kafka producer
     *
     * @return
     */
    @Override
    public CHKafkaProducer getProducer() {
        return new CHKafkaProducer(kafkaConfigHelper.configureKafkaProducer());
    }
}
