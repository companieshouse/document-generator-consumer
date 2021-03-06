package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import uk.gov.companieshouse.document.generation.request.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerProperties;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentRequest;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.service.GenerateDocument;
import uk.gov.companieshouse.document.generator.consumer.exception.GenerateDocumentException;
import uk.gov.companieshouse.environment.EnvironmentReader;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Service
public class GenerateDocumentImpl implements GenerateDocument {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    private RestTemplate restTemplate;

    private DocumentGeneratorConsumerProperties configuration;

    private EnvironmentReader reader;

    @Autowired
    public GenerateDocumentImpl(RestTemplate restTemplate, DocumentGeneratorConsumerProperties configuration,
                                EnvironmentReader reader) {

        this.restTemplate = restTemplate;
        this.configuration = configuration;
        this.reader = reader;
    }

    @Override
    public ResponseEntity<GenerateDocumentResponse> requestGenerateDocument(RenderSubmittedDataDocument renderSubmittedDataDocument) throws GenerateDocumentException {

        String url = configuration.getRootUri() + configuration.getBaseUrl();

        LOG.infoContext(renderSubmittedDataDocument.getUserId(), "Sending request to generate document to document" +
                " generator api", setDebugMap(renderSubmittedDataDocument));

        try {
            ResponseEntity<GenerateDocumentResponse> response = restTemplate.postForEntity(url,
                    setRequest(renderSubmittedDataDocument), GenerateDocumentResponse.class);

            return response;

        } catch (Exception e) {
            LOG.errorContext("Error occurred during api call to document-generator",
                    e, setDebugMap(renderSubmittedDataDocument));
            throw new GenerateDocumentException("An error occurred when requesting the generation" +
                   " of a document from the document generator api", e);
        }
    }

    private HttpEntity<GenerateDocumentRequest> setRequest(RenderSubmittedDataDocument renderSubmittedDataDocument) {

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", reader.getMandatoryString(DocumentGeneratorConsumerApplication.API_KEY));

        GenerateDocumentRequest generateDocumentRequest = populateDocumentRequest(renderSubmittedDataDocument);

        HttpEntity<GenerateDocumentRequest> request = new HttpEntity<>(generateDocumentRequest, headers);

        return request;
    }

    private GenerateDocumentRequest populateDocumentRequest(RenderSubmittedDataDocument renderSubmittedDataDocument) {
        GenerateDocumentRequest request = new GenerateDocumentRequest();
        request.setResourceUri(renderSubmittedDataDocument.getResource());
        request.setMimeType(renderSubmittedDataDocument.getContentType());
        request.setDocumentType(renderSubmittedDataDocument.getDocumentType());
        request.setPublicLocationRequired(true);

        return request;
    }

    private Map<String, Object> setDebugMap(RenderSubmittedDataDocument renderSubmittedDataDocument) {

        Map<String, Object> debugMap = new HashMap<>();
        debugMap.put(DocumentGeneratorConsumerApplication.RESOURCE_URI, renderSubmittedDataDocument.getResource());

        return debugMap;
    }
}
