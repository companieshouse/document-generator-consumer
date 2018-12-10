package uk.gov.companieshouse.document.generator.consumer.document.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerProperties;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentRequest;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.Links;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.impl.GenerateDocumentImpl;
import uk.gov.companieshouse.document.generator.consumer.exception.GenerateDocumentException;
import uk.gov.companieshouse.environment.EnvironmentReader;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GenerateDocumentTest {

    @InjectMocks
    private GenerateDocumentImpl generateDocument;

    @Mock
    private DocumentGeneratorConsumerProperties mockDocumentGeneratorConsumerProperties;

    @Mock
    private RestTemplate mockRestTemplate;

    @Mock
    private EnvironmentReader mockReader;

    @Test
    @DisplayName("Test that document generated when valid call made")
    void testDocumentGeneratedWhenValidCallMade() throws GenerateDocumentException {

        when(mockDocumentGeneratorConsumerProperties.getBaseUrl()).thenReturn("base_url");
        when(mockDocumentGeneratorConsumerProperties.getRootUri()).thenReturn("root_url");
        when(mockReader.getMandatoryString(anyString())).thenReturn("api_url");
        when(mockRestTemplate.postForEntity(anyString(), any(HttpEntity.class),
                eq(GenerateDocumentResponse.class))).thenReturn(createResponse());

        ResponseEntity<GenerateDocumentResponse> response =
                generateDocument.requestGenerateDocument(createDeserialisedKafkaMessage());

        assertNotNull(response.getBody());
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    @DisplayName("Test error thrown when Api call fails ")
    void tesErrorThrownWhenApiCallFails() {

        when(mockDocumentGeneratorConsumerProperties.getBaseUrl()).thenReturn("base_url");
        when(mockDocumentGeneratorConsumerProperties.getRootUri()).thenReturn("root_url");
        when(mockRestTemplate.postForEntity(anyString(), any(GenerateDocumentRequest.class),
                eq(GenerateDocumentResponse.class))).thenThrow(RestClientException.class);

        assertThrows(GenerateDocumentException.class, () ->
                generateDocument.requestGenerateDocument(createDeserialisedKafkaMessage()));
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

    private DeserialisedKafkaMessage createDeserialisedKafkaMessage() {

        DeserialisedKafkaMessage deserialisedKafkaMessage = new DeserialisedKafkaMessage();

        deserialisedKafkaMessage.setResource("testResource");
        deserialisedKafkaMessage.setResourceId("testResourceId");
        deserialisedKafkaMessage.setContentType("testContentType");
        deserialisedKafkaMessage.setDocumentType("testDocumentType");
        deserialisedKafkaMessage.setUserId("testUserId");
        deserialisedKafkaMessage.setId("testId");

        return deserialisedKafkaMessage;
    }
}
