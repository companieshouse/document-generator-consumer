package uk.gov.companieshouse.document.generator.consumer.exception;

public class MessageCreationException extends Exception {

    /**
     * Constructs a new MessageCreationException with a custom message
     *
     * @param message a custom message
     */
    public MessageCreationException(String message) {
        super(message);
    }

    /**
     * Constructs a new MessageCreationException with a custom message and specified cause
     *
     * @param message a custom message
     * @param cause the cause
     */
    public MessageCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
