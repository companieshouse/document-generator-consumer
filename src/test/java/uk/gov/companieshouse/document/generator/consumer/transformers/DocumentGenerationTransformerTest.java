package uk.gov.companieshouse.document.generator.consumer.transformers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.document.generation.request.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationStarted;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.Links;

public class DocumentGenerationTransformerTest {

    private static final String ID = "123456";
    private static final String DESCRIPTION = "test description";
    private static final String DESCRIPTION_IDENTIFIER = "test description identifier";
    private static final String DESCRIPTION_KEY = "description values key";
    private static final String DESCRIPTION_VALUE = "description values value";
    private static final String REQUESTER_ID = "test requester ID";
    private static final String DOCUMENT_LOCATION = "URL of document";
    private static final String DOCUMENT_SIZE = "1234";

    private DocumentGenerationTransformer transformer;

    @BeforeEach
    public void setUp() {
        transformer = new DocumentGenerationTransformer();
    }

    @Test
    @DisplayName("Test transformation of document generation started message")
    public void testTransformStarted() {
        DocumentGenerationStarted started = transformer.transformGenerationStarted(createRenderDocument());
        assertEquals(ID, started.getId());
        assertEquals(REQUESTER_ID, started.getRequesterId());
    }

    @Test
    @DisplayName("Test transformation of document generation completed message")
    public void testTransformCompleted() {
        DocumentGenerationCompleted completed = transformer.transformGenerationCompleted(createRenderDocument(), createResponse());
        assertEquals(ID, completed.getId());
        assertEquals(REQUESTER_ID, completed.getRequesterId());
        assertEquals(DESCRIPTION, completed.getDescription());
        assertEquals(DESCRIPTION_IDENTIFIER, completed.getDescriptionIdentifier());
        assertEquals(DOCUMENT_LOCATION, completed.getLocation());
        assertEquals(DOCUMENT_SIZE, completed.getDocumentSize());
        assertNotNull(completed.getDocumentCreatedAt());

        Map<String, String> descriptionValues = completed.getDescriptionValues();
        assertNotNull(descriptionValues);
        assertEquals(1, descriptionValues.size());
        assertEquals(DESCRIPTION_VALUE, descriptionValues.get(DESCRIPTION_KEY));
    }

    @Test
    @DisplayName("Test transformation of document generation failed message")
    public void testTransformFailed() {
        DocumentGenerationFailed failed = transformer.transformGenerationFailed(createRenderDocument(), createResponse());
        assertEquals(ID, failed.getId());
        assertEquals(REQUESTER_ID, failed.getRequesterId());
        assertEquals(DESCRIPTION, failed.getDescription());
        assertEquals(DESCRIPTION_IDENTIFIER, failed.getDescriptionIdentifier());

        Map<String, String> descriptionValues = failed.getDescriptionValues();
        assertNotNull(descriptionValues);
        assertEquals(1, descriptionValues.size());
        assertEquals(DESCRIPTION_VALUE, descriptionValues.get(DESCRIPTION_KEY));
    }

    private RenderSubmittedDataDocument createRenderDocument() {
        RenderSubmittedDataDocument renderSubmittedDataDocument = new RenderSubmittedDataDocument();
        renderSubmittedDataDocument.setId(ID);
        renderSubmittedDataDocument.setUserId(REQUESTER_ID);

        return renderSubmittedDataDocument;
    }

    private GenerateDocumentResponse createResponse() {
        GenerateDocumentResponse response = new GenerateDocumentResponse();
        response.setDescription(DESCRIPTION);
        response.setDescriptionIdentifier(DESCRIPTION_IDENTIFIER);
        response.setSize(DOCUMENT_SIZE);

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put(DESCRIPTION_KEY, DESCRIPTION_VALUE);
        response.setDescriptionValues(descriptionValues);

        Links links = new Links();
        links.setLocation(DOCUMENT_LOCATION);
        response.setLinks(links);

        return response;
    }
}
