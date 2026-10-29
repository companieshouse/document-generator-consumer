package uk.gov.companieshouse.document.generator.consumer.document.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.document.generation.request.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationStarted;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.Links;
import uk.gov.companieshouse.document.generator.consumer.document.service.impl.MessageServiceImpl;
import uk.gov.companieshouse.document.generator.consumer.exception.MessageCreationException;
import uk.gov.companieshouse.document.generator.consumer.transformers.DocumentGenerationTransformer;
import uk.gov.companieshouse.kafka.exceptions.SerializationException;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.serialization.AvroSerializer;
import uk.gov.companieshouse.kafka.serialization.SerializerFactory;

@ExtendWith(MockitoExtension.class)
public class MessageServiceImplTest {

    @InjectMocks
    private MessageServiceImpl messageService;

    @Mock
    private SerializerFactory serializerFactory;

    @Mock
    private AvroSerializer<DocumentGenerationStarted> startedSerializer;

    @Mock
    private AvroSerializer<DocumentGenerationCompleted> completedSerializer;

    @Mock
    private AvroSerializer<DocumentGenerationFailed> failedSerializer;

    @Mock
    private DocumentGenerationTransformer transformer;

    @Mock
    private DocumentGenerationStarted documentGenerationStarted;

    @Mock
    private DocumentGenerationCompleted documentGenerationCompleted;

    @Mock
    private DocumentGenerationFailed documentGenerationFailed;

    private static final String STARTED_PRODUCER_TOPIC = "document-generation-started";
    private static final String FAILED_PRODUCER_TOPIC = "document-generation-failed";
    private static final String COMPLETED_PRODUCER_TOPIC = "document-generation-completed";

    private static final byte[] STARTED_BYTES = "Started document generation message".getBytes();
    private static final byte[] FAILED_BYTES = "Completed document generation message".getBytes();
    private static final byte[] COMPLETED_BYTES = "Failed document generation message".getBytes();

    @Test
    @DisplayName("Test that a create document started message is generated")
    void createDocumentGenerationStartedTest() throws Exception {

        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();

        when(transformer.transformGenerationStarted(renderSubmittedDataDocument)).thenReturn(documentGenerationStarted);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationStarted.class)).thenReturn(startedSerializer);
        when(startedSerializer.toBinary(documentGenerationStarted)).thenReturn(STARTED_BYTES);

        Message message = messageService.createDocumentGenerationStarted(renderSubmittedDataDocument);

        assertMessageGenerated(message, STARTED_PRODUCER_TOPIC, STARTED_BYTES);
    }

    @Test
    @DisplayName("Test that a create document started message throws exception")
    void createDocumentGenerationStartedExceptionTest() throws Exception {

        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();
        when(transformer.transformGenerationStarted(renderSubmittedDataDocument)).thenReturn(documentGenerationStarted);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationStarted.class)).thenReturn(startedSerializer);
        when(startedSerializer.toBinary(documentGenerationStarted)).thenThrow(new SerializationException("error"));

        assertThrows(MessageCreationException.class,
                () -> messageService.createDocumentGenerationStarted(renderSubmittedDataDocument));
    }

    @Test
    @DisplayName("Test that a create document failed message is generated with populated generateDocumentResponse")
    void createDocumentGenerationFailedPopulatedDocumentResponseTest() throws Exception {
        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();
        GenerateDocumentResponse generateDocumentResponse = createResponse();

        when(transformer.transformGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse)).thenReturn(
                documentGenerationFailed);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationFailed.class)).thenReturn(failedSerializer);
        when(failedSerializer.toBinary(documentGenerationFailed)).thenReturn(FAILED_BYTES);

        Message message = messageService.createDocumentGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse);

        assertMessageGenerated(message, FAILED_PRODUCER_TOPIC, FAILED_BYTES);
    }

    @Test
    @DisplayName("Test that a create document failed message is generated with null generateDocumentResponse")
    void createDocumentGenerationFailedNullDocumentResponseTest() throws Exception {
        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();
        GenerateDocumentResponse generateDocumentResponse = null;

        when(transformer.transformGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse)).thenReturn(
                documentGenerationFailed);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationFailed.class)).thenReturn(failedSerializer);
        when(failedSerializer.toBinary(documentGenerationFailed)).thenReturn(FAILED_BYTES);

        Message message = messageService.createDocumentGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse);

        assertMessageGenerated(message, FAILED_PRODUCER_TOPIC, FAILED_BYTES);
    }

    @Test
    @DisplayName("Test that a create document failed message is generated with null generateDocumentResponse && null deserialised kafka message")
    void createDocumentGenerationFailedNullDocumentResponseAndKafkaMessageTest() throws Exception {
        RenderSubmittedDataDocument renderSubmittedDataDocument = null;
        GenerateDocumentResponse generateDocumentResponse = null;

        when(transformer.transformGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse)).thenReturn(
                documentGenerationFailed);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationFailed.class)).thenReturn(failedSerializer);
        when(failedSerializer.toBinary(documentGenerationFailed)).thenReturn(FAILED_BYTES);

        Message message = messageService.createDocumentGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse);

        assertMessageGenerated(message, FAILED_PRODUCER_TOPIC, FAILED_BYTES);
    }

    @Test
    @DisplayName("Test that a create document failed message throws exception")
    void createDocumentGenerationFailedExceptionTest() throws Exception {
        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();
        GenerateDocumentResponse generateDocumentResponse = createResponse();

        when(transformer.transformGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse)).thenReturn(
                documentGenerationFailed);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationFailed.class)).thenReturn(failedSerializer);
        when(failedSerializer.toBinary(documentGenerationFailed)).thenThrow(new SerializationException("error"));

        assertThrows(MessageCreationException.class,
                () -> messageService.createDocumentGenerationFailed(renderSubmittedDataDocument, generateDocumentResponse));
    }

    @Test
    @DisplayName("Test that a create document completed message is generated")
    void createDocumentGenerationCompletedTest() throws Exception {
        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();
        GenerateDocumentResponse generateDocumentResponse = createResponse();

        when(transformer.transformGenerationCompleted(renderSubmittedDataDocument, generateDocumentResponse)).thenReturn(
                documentGenerationCompleted);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationCompleted.class)).thenReturn(completedSerializer);
        when(completedSerializer.toBinary(documentGenerationCompleted)).thenReturn(COMPLETED_BYTES);

        Message message = messageService.createDocumentGenerationCompleted(renderSubmittedDataDocument, generateDocumentResponse);

        assertMessageGenerated(message, COMPLETED_PRODUCER_TOPIC, COMPLETED_BYTES);
    }

    @Test
    @DisplayName("Test that a create document completed message throws exception")
    void createDocumentGenerationCompletedExceptionTest() throws Exception {
        RenderSubmittedDataDocument renderSubmittedDataDocument = createRenderSubmittedDataDocument();
        GenerateDocumentResponse generateDocumentResponse = createResponse();

        when(transformer.transformGenerationCompleted(renderSubmittedDataDocument, generateDocumentResponse)).thenReturn(
                documentGenerationCompleted);
        when(serializerFactory.getSpecificRecordSerializer(DocumentGenerationCompleted.class)).thenReturn(completedSerializer);
        when(completedSerializer.toBinary(documentGenerationCompleted)).thenThrow(new SerializationException("error"));

        assertThrows(MessageCreationException.class,
                () -> messageService.createDocumentGenerationCompleted(renderSubmittedDataDocument, generateDocumentResponse));
    }

    /**
     * Populate GenerateDocumentResponse with content
     */
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

    /**
     * Populate RenderSubmittedDataDocument with content
     *
     * @return
     */
    private RenderSubmittedDataDocument createRenderSubmittedDataDocument() {
        RenderSubmittedDataDocument renderSubmittedDataDocument = new RenderSubmittedDataDocument();
        renderSubmittedDataDocument.setContentType("contentType");
        renderSubmittedDataDocument.setDocumentType("documentType");
        renderSubmittedDataDocument.setId("id");
        renderSubmittedDataDocument.setResource("resource");
        renderSubmittedDataDocument.setUserId("userId");

        return renderSubmittedDataDocument;
    }

    /**
     * Assert message is not null and assert that message topic and message value match
     *
     * @param message
     * @param producerTopic
     * @param valueBytes
     */
    private void assertMessageGenerated(Message message, String producerTopic, byte[] valueBytes) {
        assertNotNull(message);
        assertEquals(producerTopic, message.getTopic());
        assertEquals(valueBytes, message.getValue());
    }
}
