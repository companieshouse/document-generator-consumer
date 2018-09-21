package uk.gov.companieshouse.document.generator.consumer.document;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerProducerHandler;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Arrays;

public class DocumentGeneratorConsumer {

    private static final Logger LOG = LoggerFactory.getLogger("document-generator-consumer");

    private static final String CONSUMER_TOPIC_VAR = "CONSUMER_TOPIC";
    private static final String GROUP_NAME_VAR = "GROUP_NAME";

    @Autowired
    private MessageService messageService;

    private CHKafkaConsumerGroup consumerGroup;

    private KafkaConsumerProducerHandler kafkaConsumerProducerHandler;

    private EnvironmentReader environmentReader;

    private AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer;

    @Autowired
    public DocumentGeneratorConsumer(KafkaConsumerProducerHandler kafkaConsumerProducerHandler,
                                     EnvironmentReader environmentReader,
                                     MessageService messageService,
                                     AvroDeserializer<DeserialisedKafkaMessage> avroDeserializer) {

        this.kafkaConsumerProducerHandler = kafkaConsumerProducerHandler;
        this.environmentReader = environmentReader;
        this.messageService = messageService;
        this.avroDeserializer = avroDeserializer;

        consumerGroup = kafkaConsumerProducerHandler.getConsumerGroup(Arrays.asList(
                environmentReader.getMandatoryString(CONSUMER_TOPIC_VAR)),
                environmentReader.getMandatoryString(GROUP_NAME_VAR));
    }

    public void pollAndGenerateDocument() throws MessageCreationException {

        for(Message message : consumerGroup.consume()) {
            DeserialisedKafkaMessage deserialisedKafkaMessage = null;

            try {
                deserialisedKafkaMessage = avroDeserializer.deserialize(message, DeserialisedKafkaMessage.getClassSchema());
                messageService.createDocumentGenerationStarted(deserialisedKafkaMessage);
            } catch (Exception e) {
                messageService.createDocumentGenerationFailed(deserialisedKafkaMessage, null);
                consumerGroup.commit();
                LOG.error(e);
                throw new MessageCreationException(e.getMessage(), e.getCause());
            }
        }
    }
}
