package uk.gov.companieshouse.document.generator.consumer.kafka;

import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

import java.util.List;

public interface KafkaConsumerProducerHandler {
    /**
     * get the consumerGroup
     *
     * @param consumerTopics
     * @param groupName
     * @return
     */
    CHKafkaConsumerGroup getConsumerGroup(List<String> consumerTopics, String groupName);

    /**
     * Get kafka producer
     *
     * @return
     */
    CHKafkaProducer getProducer();
}
