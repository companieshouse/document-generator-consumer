package uk.gov.companieshouse.document.generator.consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import uk.gov.companieshouse.kafka.deserialization.DeserializerFactory;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@Configuration
@ComponentScan(basePackages = {"uk.gov.companieshouse.kafka.serialization"})
public class DocumentGeneratorConsumerApplicationConfiguration {

    @Bean
    SerializerFactory serializerFactory() {
        return new SerializerFactory();
    }

    @Bean
    DeserializerFactory deserializerFactory() {
        return new DeserializerFactory();
    }
}
