package uk.gov.companieshouse.document.generator.consumer.configuration;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;

@Configuration
public class KafkaConfiguration {

    @Bean
    public ConsumerConfig kafkaConsumerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.topic}") String topicName,
            @Value("${spring.kafka.consumer.groupId}") String groupName) {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setTopics(Collections.singletonList(topicName));
        consumerConfig.setGroupName(groupName);
        consumerConfig.setResetOffset(false);
        consumerConfig.setBrokerAddresses(bootstrapServers.split(","));

        //ConsumerConfigHelper.assignBrokerAddresses(consumerConfig);

        return consumerConfig;
    }

    @Bean
    public ProducerConfig kafkaProducerConfig(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        ProducerConfig producerConfig = new ProducerConfig();

        producerConfig.setRoundRobinPartitioner(true);
        producerConfig.setAcks(Acks.WAIT_FOR_ALL);
        producerConfig.setRetries(10);
        producerConfig.setEnableIdempotence(false);
        producerConfig.setBrokerAddresses(bootstrapServers.split(","));

        //ProducerConfigHelper.assignBrokerAddresses(producerConfig);

        return producerConfig;
    }
}
