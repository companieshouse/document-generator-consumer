package uk.gov.companieshouse.document.generator.consumer.exception;

public class GenerateDocumentException extends Exception {

    /**
     * Constructs a new GenerateDocumentException with a custom message
     *
     * @param message a custom message
     */
    public GenerateDocumentException(String message) {
        super(message);
    }

    /**
     * Constructs a new GenerateDocumentException with a custom message and specified cause
     *
     * @param message a custom message
     * @param cause the cause
     */
    public GenerateDocumentException(String message, Throwable cause) {
        super(message, cause);
    }
}