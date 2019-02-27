package net.brutus5000.deltaforge.client.error;

/**
 * {@code CheckoutException} can be thrown on errors during checkout of a tag.
 */
public class CheckoutException extends RuntimeException {
    public CheckoutException(String message) {
        super(message);
    }

    public CheckoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
