package net.brutus5000.deltaforge.client.error;

import com.github.jasminb.jsonapi.models.errors.Error;

import java.util.List;
import java.util.stream.Collectors;

public class ApiException extends RuntimeException {
    private final List<Error> errors;

    public ApiException(List<Error> errors) {
        this.errors = errors;
    }

    public String getLocalizedMessage() {
        return (String) this.errors.stream().map(Error::getDetail).collect(Collectors.joining("\n"));
    }
}