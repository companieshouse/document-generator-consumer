package uk.gov.companieshouse.document.generator.consumer.configuration;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessor;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessorRunner;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfig;
import uk.gov.companieshouse.kafka.consumer.ConsumerConfigHelper;

@Configuration
public class MessageProcessorConfiguration {

    @Bean
    public MessageProcessorRunner messageProcessorRunner() {
        return new MessageProcessorRunner();
    }

}
