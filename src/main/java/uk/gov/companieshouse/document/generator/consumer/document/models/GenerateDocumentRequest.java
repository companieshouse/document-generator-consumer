package uk.gov.companieshouse.document.generator.consumer.document.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.Gson;

public class GenerateDocumentRequest {

    @JsonProperty("resource_uri")
    private String resourceUri;

    @JsonProperty("mime_type")
    private String mimeType;

    @JsonProperty("document_type")
    private String documentType;

    @JsonProperty("is_public_location_required")
    private boolean isPublicLocationRequired;

    public String getResourceUri() {
        return resourceUri;
    }

    public void setResourceUri(String resourceUri) {
        this.resourceUri = resourceUri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public boolean isPublicLocationRequired() {
        return isPublicLocationRequired;
    }

    public void setPublicLocationRequired(boolean publicLocationRequired) {
        isPublicLocationRequired = publicLocationRequired;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
