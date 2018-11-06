package uk.gov.companieshouse.document.generator.consumer.document.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerApplication;
import uk.gov.companieshouse.document.generator.consumer.DocumentGeneratorConsumerProperties;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentRequest;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;
import uk.gov.companieshouse.document.generator.consumer.document.models.avro.DeserialisedKafkaMessage;
import uk.gov.companieshouse.document.generator.consumer.document.service.GenerateDocument;
import uk.gov.companieshouse.document.generator.consumer.exception.GenerateDocumentException;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Service
public class GenerateDocumentImpl implements GenerateDocument {

    private static final Logger LOG = LoggerFactory.getLogger(DocumentGeneratorConsumerApplication.APPLICATION_NAME_SPACE);

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DocumentGeneratorConsumerProperties configuration;

    @Override
    public ResponseEntity requestGenerateDocument(DeserialisedKafkaMessage deserialisedKafkaMessage) throws GenerateDocumentException {

        String url = configuration.getRootUri() + configuration.getBaseUrl();

        GenerateDocumentRequest request = populateDocumentRequest(deserialisedKafkaMessage);

        LOG.infoContext(deserialisedKafkaMessage.getUserId(), "Sending request to generate document to document" +
                " generator api", setDebugMap(deserialisedKafkaMessage));

        try {
            ResponseEntity<GenerateDocumentResponse> response = restTemplate.postForEntity(url, request,
                    GenerateDocumentResponse.class);

            return response;

        } catch (Exception e) {
           throw new GenerateDocumentException("An error occurred when requesting the generation" +
                   " of a document from the document generator api", e);
        }
    }

    private GenerateDocumentRequest populateDocumentRequest(DeserialisedKafkaMessage deserialisedKafkaMessage) {
        GenerateDocumentRequest request = new GenerateDocumentRequest();
        request.setResourceUri(deserialisedKafkaMessage.getResourceId());
        request.setResourceID(deserialisedKafkaMessage.getResource());
        request.setMimeType(deserialisedKafkaMessage.getContentType());
        request.setDocumentType(deserialisedKafkaMessage.getDocumentType());
        return request;
    }

    private Map<String, Object> setDebugMap(DeserialisedKafkaMessage deserialisedKafkaMessage) {

        Map<String, Object> debugMap = new HashMap<>();
        debugMap.put(DocumentGeneratorConsumerApplication.RESOURCE_URI, deserialisedKafkaMessage.getResource());
        debugMap.put(DocumentGeneratorConsumerApplication.RESOURCE_ID, deserialisedKafkaMessage.getResourceId());

        return debugMap;
    }
}
