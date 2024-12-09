package uk.gov.companieshouse.document.generator.consumer.kafka.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaProducerService;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.Acks;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.kafka.producer.ProducerConfig;
import uk.gov.companieshouse.kafka.producer.ProducerConfigHelper;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.concurrent.ExecutionException;

@Service
public class KafkaProducerServiceImpl implements KafkaProducerService {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private CHKafkaProducer producer;
    private ProducerConfig config;

    public KafkaProducerServiceImpl() {

        LOG.debug("Creating kafka producer service " + this.toString());

        ProducerConfig producerConfig = new ProducerConfig();

        producerConfig.setRoundRobinPartitioner(true);
        producerConfig.setAcks(Acks.WAIT_FOR_ALL);
        producerConfig.setRetries(10);

        ProducerConfigHelper.assignBrokerAddresses(producerConfig);
        producer = new CHKafkaProducer(producerConfig);

        config = new ProducerConfig();
        config.setEnableIdempotence(false);
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
