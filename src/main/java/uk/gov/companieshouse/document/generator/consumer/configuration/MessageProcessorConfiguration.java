package uk.gov.companieshouse.document.generator.consumer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.document.generator.consumer.processor.MessageProcessorRunner;

@Configuration
public class MessageProcessorConfiguration {

    @Bean
    public MessageProcessorRunner messageProcessorRunner() {
        return new MessageProcessorRunner();
    }
}
