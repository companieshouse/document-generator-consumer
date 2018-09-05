package uk.gov.companieshouse.document.generator.consumer.avro;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.document.generator.consumer.document.models.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generator.consumer.document.models.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generator.consumer.document.models.DocumentGenerationStarted;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class DocumentGenerationStateAvroSerializerTest {

    private static final String STARTED_ENCODED_AVRO_STRING = "\f123456test guid";
    private static final String COMPLETED_ENCODED_AVRO_STRING = "\f123456test guid test description," +
            "description identifier22017-05-22T00:00:00+01:00date01/01/1980 test-location\n1234L";
    private static final String FAILED_ENCODED_AVRO_STRING = "\f123456  test description ," +
            "description identifier\nan-id\bdate01/01/1980 ";
    private static final String FAILED_ENCODED_MANDATORY_AVRO_STRING = "\f123456\nan-id\bdate01/01/1980 ";



    @Test
    @DisplayName("Serialize data for document generation started")
    public void testSerializeDocumentGenerationStarted() throws IOException {

        DocumentGenerationStarted document = new DocumentGenerationStarted();
        document.setId("test guid");
        document.setRequesterId("123456");

        DocumentGenerationStateAvroSerializer serializer = new DocumentGenerationStateAvroSerializer();
        byte[] result = serializer.serialize(document);
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
