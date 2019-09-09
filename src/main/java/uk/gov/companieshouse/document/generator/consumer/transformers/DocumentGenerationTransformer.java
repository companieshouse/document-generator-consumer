package uk.gov.companieshouse.document.generator.consumer.transformers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Component;

import uk.gov.companieshouse.document.generation.request.RenderSubmittedDataDocument;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationCompleted;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationFailed;
import uk.gov.companieshouse.document.generation.status.DocumentGenerationStarted;
import uk.gov.companieshouse.document.generator.consumer.document.models.GenerateDocumentResponse;

@Component
public class DocumentGenerationTransformer {

    private DateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Transform the document generation request into a document generation started message
     * 
     * @return started
     */
    public DocumentGenerationStarted transformGenerationStarted(RenderSubmittedDataDocument renderSubmittedDataDocument) {
        DocumentGenerationStarted started = new DocumentGenerationStarted();
        started.setId(renderSubmittedDataDocument.getId());
        started.setRequesterId(renderSubmittedDataDocument.getUserId());

        return started;
    }

    /**
     * Transform the document generation request into a document generation completed message
     * 
     * @return completed
     */
    public DocumentGenerationCompleted transformGenerationCompleted(RenderSubmittedDataDocument renderSubmittedDataDocument, GenerateDocumentResponse response) {
         DocumentGenerationCompleted completed = new DocumentGenerationCompleted();
         completed.setId(renderSubmittedDataDocument.getId());
         completed.setRequesterId(renderSubmittedDataDocument.getUserId());
         completed.setDescription(response.getDescription());
         completed.setDescriptionIdentifier(response.getDescriptionIdentifier());
         completed.setLocation(response.getLinks().getLocation());
         completed.setDocumentSize(response.getSize());
         completed.setDocumentCreatedAt(isoDateFormat.format(new Date(System.currentTimeMillis())));
         completed.setDescriptionValues(response.getDescriptionValues());

         return completed;
    }

    /**
     * Transform the document generation request into a document generation failed message
     * 
     * @return failed
     */
    public DocumentGenerationFailed transformGenerationFailed(RenderSubmittedDataDocument renderSubmittedDataDocument, GenerateDocumentResponse response) {
        DocumentGenerationFailed failed = new DocumentGenerationFailed();
        failed.setId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getId() : "");
        failed.setRequesterId(renderSubmittedDataDocument != null ? renderSubmittedDataDocument.getUserId() : "");
        failed.setDescription(response != null ? response.getDescription() : "");
        failed.setDescriptionIdentifier(response != null ? response.getDescriptionIdentifier() : "");

        if (response != null && response.getDescriptionValues() != null) {
            failed.setDescriptionValues(response.getDescriptionValues());
        }

        return failed;
    }
}
