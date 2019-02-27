package net.brutus5000.deltaforge.client.error;

/**
 * {@code InitializeException} can be thrown on errors during initialization of a new repository.
 */
public class InitializeException extends Exception {
    public InitializeException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitializeException(String message) {
        super(message);
    }
}
