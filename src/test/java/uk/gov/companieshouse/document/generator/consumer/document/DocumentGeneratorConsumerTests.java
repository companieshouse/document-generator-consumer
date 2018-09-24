package uk.gov.companieshouse.document.generator.consumer.document;

import org.apache.avro.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerProducerHandler;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DocumentGeneratorConsumerTests {

    @InjectMocks
    private DocumentGeneratorConsumer documentGeneratorConsumer;

    @Mock
    private CHKafkaConsumerGroup mockConsumerGroup;

    @Mock
    private KafkaConsumerProducerHandler mockKafkaConsumerProducerHandler;

    @Mock
    private EnvironmentReader mockEnvironmentReader;

    @Mock
    private MessageService mockMessageService;

    @Mock
    private DeserialisedKafkaMessage mockDeserialisedKafkaMessage;

    @Mock
    private AvroDeserializer<DeserialisedKafkaMessage> mockAvroDeserializer;

    List<Message> messages;
    Message message;

    @BeforeEach
    void init () {
        when(mockEnvironmentReader.getMandatoryString(any(String.class))).thenReturn("string");
        when(mockKafkaConsumerProducerHandler.getConsumerGroup(anyList(), any(String.class))).thenReturn(mockConsumerGroup);
        when(mockConsumerGroup.consume()).thenReturn(createTestMessageList());

        documentGeneratorConsumer = new DocumentGeneratorConsumer(mockKafkaConsumerProducerHandler,
                mockEnvironmentReader, mockMessageService, mockAvroDeserializer);
    }

    @Test
    @DisplayName("Test message for create document generation started ")
    void pollAndGenerateStartedMessageCreatedTest() throws Exception {
        when(mockAvroDeserializer.deserialize(any(Message.class), any(Schema.class))).thenReturn(mockDeserialisedKafkaMessage);
        documentGeneratorConsumer.pollAndGenerateDocument();

        assertEquals(any(Message.class), mockMessageService.createDocumentGenerationStarted(mockDeserialisedKafkaMessage));
    }

    @Test
    @DisplayName("Test message for create document generation failed")
    void pollAndGenerateFailedMessageVerifiedTest() throws Exception {
        when(mockAvroDeserializer.deserialize(any(Message.class), any(Schema.class))).thenThrow(new IOException());
        documentGeneratorConsumer.pollAndGenerateDocument();

        verify(mockMessageService).createDocumentGenerationFailed(null, null);
    }

    private List< Message > createTestMessageList() {
        messages = new ArrayList< >();
        message = new Message();
        message.setKey("test key");
        message.setOffset(100L);
        message.setPartition(123);
        message.setTimestamp(new Date().getTime());
        message.setTopic("document-generation-started");
        message.setValue("value 1".getBytes());
        messages.add(message);

        return messages;
    }
}
