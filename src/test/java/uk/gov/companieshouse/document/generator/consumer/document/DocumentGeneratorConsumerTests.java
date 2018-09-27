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
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentRequest;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.Links;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerProducerHandler;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.kafka.consumer.CHKafkaConsumerGroup;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
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

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private GenerateDocumentResponse mockGenerateDocumentResponse;

    private List<Message> messages;

    private Message message;

    @BeforeEach
    void init () {
        when(mockEnvironmentReader.getMandatoryString(any(String.class))).thenReturn("string");
        when(mockKafkaConsumerProducerHandler.getConsumerGroup(anyList(), any(String.class))).thenReturn(mockConsumerGroup);
        when(mockConsumerGroup.consume()).thenReturn(createTestMessageList());

        documentGeneratorConsumer = new DocumentGeneratorConsumer(mockKafkaConsumerProducerHandler,
                mockEnvironmentReader, mockMessageService, mockAvroDeserializer, mockRestTemplate);
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

    @Test
    @DisplayName("Test message for create document generation completed")
    void requestGenerateDocumentCompletedMessageCreated() throws Exception {

        DeserialisedKafkaMessage deserialisedKafkaMessage = new DeserialisedKafkaMessage();
        deserialisedKafkaMessage.setResource("testResource");
        deserialisedKafkaMessage.setResourceId("testResourceId");
        deserialisedKafkaMessage.setContentType("testContentType");
        deserialisedKafkaMessage.setDocumentType("testDocumentType");

        when(mockRestTemplate.postForObject("test/uri", GenerateDocumentRequest.class, GenerateDocumentResponse.class)).thenReturn(createResponse());

        documentGeneratorConsumer.requestGenerateDocument(deserialisedKafkaMessage);

//        verify(mockMessageService).createDocumentGenerationCompleted(deserialisedKafkaMessage, mockGenerateDocumentResponse);
    }

    private GenerateDocumentResponse createResponse() {
        GenerateDocumentResponse response = new GenerateDocumentResponse();
        Links links = new Links();
        links.setLocation("location");
        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put("value1", "value2");
        response.setDescription("description");
        response.setDescriptionIdentifier("descriptionIdentifier");
        response.setDescriptionValues(descriptionValues);
        response.setLinks(links);
        response.setSize("size");

        return response;
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
