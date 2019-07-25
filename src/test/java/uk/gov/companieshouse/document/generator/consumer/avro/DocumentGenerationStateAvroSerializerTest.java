package uk.gov.companieshouse.document.generator.consumer.avro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.document.generation.status.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationStarted;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DocumentGenerationStateAvroSerializerTest {

    private static final String STARTED_ENCODED_AVRO_STRING = "\f123456\u0012test guid";
    private static final String COMPLETED_ENCODED_AVRO_STRING = "\f123456\u0012test guid test description,description " +
            "identifier22017-05-22T00:00:00+01:00\u0002\bdate\u001401/01/1980\u0000\u001Atest-location\n1234L";
    private static final String FAILED_ENCODED_AVRO_STRING = "\f123456\u0000 test description\u0000,description identifier\n" +
            "an-id\u0002\u0002\bdate\u0002\u001401/01/1980\u0000";
    private static final String FAILED_ENCODED_MANDATORY_AVRO_STRING = "\f123456\u0002\u0002\n" +
            "an-id\u0002\u0002\bdate\u0002\u001401/01/1980\u0000";

    @Test
    @DisplayName("Serialize data for document generation started")
    public void testSerializeDocumentGenerationStarted() throws IOException {

        DocumentGenerationStarted document = new DocumentGenerationStarted();
        document.setId("test guid");
        document.setRequesterId("123456");

        DocumentGenerationStateAvroSerializer serializer = new DocumentGenerationStateAvroSerializer();
        byte[] result = serializer.serialize(document);
        String tests = new String(result);

        assertEquals(STARTED_ENCODED_AVRO_STRING, new String(result));
    }

    @Test
    @DisplayName("Serialize data for document generation completed")
    public void testSerializeDocumentGenerationCompleted() throws IOException{

        DocumentGenerationCompleted document = new DocumentGenerationCompleted();
        document.setLocation("test-location");
        document.setId("test guid");
        document.setDescription("test description");
        document.setDescriptionIdentifier("description identifier");
        document.setRequesterId("123456");
        document.setDocumentCreatedAt("2017-05-22T00:00:00+01:00");
        document.setDocumentSize("1234L");

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put("date", "01/01/1980");

        document.setDescriptionValues(descriptionValues);

        DocumentGenerationStateAvroSerializer serializer = new DocumentGenerationStateAvroSerializer();
        byte[] result = serializer.serialize(document);
        String tests = new String(result);

        assertEquals(COMPLETED_ENCODED_AVRO_STRING, new String(result));
    }

    @Test
    @DisplayName("Serialize data for document generation failed")
    public void testSerializeDocumentGenerationFailed() throws IOException {

        DocumentGenerationFailed document = new DocumentGenerationFailed();
        document.setDescription("test description");
        document.setDescriptionIdentifier("description identifier");
        document.setRequesterId("123456");
        document.setId("an-id");

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put("date", "01/01/1980");

        document.setDescriptionValues(descriptionValues);

        DocumentGenerationStateAvroSerializer serializer = new DocumentGenerationStateAvroSerializer();
        byte[] result = serializer.serialize(document);
        String tests = new String(result);

        assertEquals(FAILED_ENCODED_AVRO_STRING, new String(result));
    }

    @Test
    @DisplayName("Serialize data for document generation failed mandatory fields only")
    public void testSerializeDocumentGenerationFailedMandatoryFieldsOnly() throws IOException {

        DocumentGenerationFailed document = new DocumentGenerationFailed();
        document.setRequesterId("123456");
        document.setId("an-id");

        Map<String, String> descriptionValues = new HashMap<>();
        descriptionValues.put("date", "01/01/1980");

        document.setDescriptionValues(descriptionValues);

        DocumentGenerationStateAvroSerializer serializer = new DocumentGenerationStateAvroSerializer();
        byte[] result = serializer.serialize(document);

        assertEquals(FAILED_ENCODED_MANDATORY_AVRO_STRING, new String(result));
    }
}
