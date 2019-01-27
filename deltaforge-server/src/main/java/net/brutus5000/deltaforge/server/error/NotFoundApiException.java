package net.brutus5000.deltaforge.server.error;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class NotFoundApiException extends ApiException {
    public NotFoundApiException(Error error) {
        super(error);
    }

    public NotFoundApiException(Error[] errors) {
        super(errors);
    }
}
