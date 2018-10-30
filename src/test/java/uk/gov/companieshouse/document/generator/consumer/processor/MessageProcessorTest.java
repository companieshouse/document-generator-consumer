package uk.gov.companieshouse.document.generator.consumer.processor;


import org.apache.avro.Schema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.document.generator.consumer.avro.AvroDeserializer;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.Links;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.GenerateDocument;
import uk.gov.companieshouse.document.generator.consumer.document.service.MessageService;
import uk.gov.companieshouse.document.generator.consumer.exception.GenerateDocumentException;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaConsumerService;
import uk.gov.companieshouse.document.generator.consumer.kafka.KafkaProducerService;
import uk.gov.companieshouse.document.generator.consumer.processor.impl.MessageProcessorImpl;
import uk.gov.companieshouse.kafka.message.Message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MessageProcessorTest {

    @InjectMocks
    private MessageProcessorImpl messageProcessor;

    @Mock
    private KafkaConsumerService mockKafkaConsumerService;

    @Mock
    private KafkaProducerService mockKafkaProducerService;

    @Mock
    private AvroDeserializer<DeserialisedKafkaMessage> mockAvroDeserializer;

    @Mock
    private GenerateDocument mockGenerateDocument;

    @Mock
    private MessageService mockMessageService;

    @Mock
    private DeserialisedKafkaMessage mockDeserialisedKafkaMessage;

    private List<Message> messages;

    private Message message;

    @Test
    @DisplayName("Test started and completed message generated on valid request")
    public void testsMessageProcessedCreatesStartedAndCompletedMessage() throws IOException, GenerateDocumentException,
            InterruptedException, MessageCreationException, ExecutionException {

        when(mockKafkaConsumerService.consume()).thenReturn(createTestMessageList());
        when(mockAvroDeserializer.deserialize(any(Message.class), any(Schema.class)))
                .thenReturn(createDeserialisedKafkaMessage());
        when(mockGenerateDocument.requestGenerateDocument(any(DeserialisedKafkaMessage.class)))
                .thenReturn(createResponse());

        messageProcessor.processKafkaMessage();

        assertEquals(any(Message.class), mockMessageService.createDocumentGenerationStarted(mockDeserialisedKafkaMessage));
        assertEquals(any(Message.class), mockMessageService.createDocumentGenerationCompleted(
                createDeserialisedKafkaMessage(), any(GenerateDocumentResponse.class)));
    }

    @Test
    @DisplayName("Test failed message generated on error")
    public void testsMessageProcessedCreatesFailedMessageOnError() throws IOException, InterruptedException,
            MessageCreationException, ExecutionException {

        when(mockKafkaConsumerService.consume()).thenReturn(createTestMessageList());
        when(mockAvroDeserializer.deserialize(any(Message.class), any(Schema.class)))
                .thenThrow(new IOException());

        messageProcessor.processKafkaMessage();

        assertEquals(any(Message.class), mockMessageService.createDocumentGenerationFailed(
                createDeserialisedKafkaMessage(), any(GenerateDocumentResponse.class)));
    }

    private List<Message> createTestMessageList() {

        messages = new ArrayList<>();
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

    private DeserialisedKafkaMessage createDeserialisedKafkaMessage() {

        DeserialisedKafkaMessage deserialisedKafkaMessage = new DeserialisedKafkaMessage();

        deserialisedKafkaMessage.setResource("testResource");
        deserialisedKafkaMessage.setResourceId("testResourceId");
        deserialisedKafkaMessage.setContentType("testContentType");
        deserialisedKafkaMessage.setDocumentType("testDocumentType");

        return deserialisedKafkaMessage;
    }

    private ResponseEntity createResponse() {

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

        ResponseEntity responseEntity = new ResponseEntity<>(response, HttpStatus.CREATED);

        return responseEntity;
    }
}
