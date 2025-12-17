package uk.gov.companieshouse.document.generator.consumer.kafka.impl;

import java.util.concurrent.ExecutionException;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaProducerService;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private final CHKafkaProducer producer;

    public KafkaProducerServiceImpl(final ProducerConfig producerConfig) {
        LOG.debug("Creating kafka producer service " + this.toString());

        producer = new CHKafkaProducer(producerConfig);
    }

    @Override
    public void send(Message kafkaMessage) throws ExecutionException, InterruptedException {
        LOG.debug("Sending kafka message value " + kafkaMessage + " to topic " + kafkaMessage.getTopic());

        producer.send(kafkaMessage);
    }

    @Override
    public void close() {
        LOG.debug("Closing kafka producer service " + this.toString());

        producer.close();
    }
}
