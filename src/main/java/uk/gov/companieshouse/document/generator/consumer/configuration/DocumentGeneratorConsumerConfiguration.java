package uk.gov.companieshouse.document.generator.consumer.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerProperties;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.DocumentGeneratorConsumer;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerProducerHandler;
import uk.gov.companieshouse.environment.EnvironmentReader;

@Configuration
public class DocumentGeneratorConsumerConfiguration {

    @Autowired
    private MessageService messageService;

    private RestTemplate restTemplate;

    @Autowired
    private KafkaConsumerProducerHandler kafkaConsumerProducerHandler;

    @Autowired
    private EnvironmentReader environmentReader;

    @Autowired
    private AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer;

    @Autowired
    private DocumentGeneratorConsumerProperties configuration;


    @Bean
    public DocumentGeneratorConsumer documentGeneratorConsumer() {

        return new DocumentGeneratorConsumer(kafkaConsumerProducerHandler, environmentReader, messageService,
                avroDeserializer, restTemplate, configuration);
    }

}
