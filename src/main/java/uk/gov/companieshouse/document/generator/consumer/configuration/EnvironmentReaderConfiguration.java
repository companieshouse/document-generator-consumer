package uk.gov.companieshouse.document.generator.consumer.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.environment.impl.EnvironmentReaderImpl;

@Configuration
public class EnvironmentReaderConfiguration {

    @Bean
    EnvironmentReader environmentReader() {
        return new EnvironmentReaderImpl();
    }
}
