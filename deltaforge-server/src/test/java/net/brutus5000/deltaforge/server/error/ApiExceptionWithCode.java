package net.brutus5000.deltaforge.server.error;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

import java.util.Arrays;

public final class ApiExceptionWithCode extends BaseMatcher<ApiException> {

    private final ErrorCode errorCode;
    private final boolean checkArgs;
    private final Object[] args;

    private ApiExceptionWithCode(ErrorCode errorCode, boolean checkArgs, Object... args) {
        this.errorCode = errorCode;
        this.checkArgs = checkArgs;
        this.args = args;
    }

    public static ApiExceptionWithCode apiExceptionWithCode(ErrorCode errorCode, Object... args) {
        return new ApiExceptionWithCode(errorCode, false, args);
    }

    public static ApiExceptionWithCode apiExceptionWithCodeAndArgs(ErrorCode errorCode, Object... args) {
        return new ApiExceptionWithCode(errorCode, true, args);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("an ApiException with exactly one error: " + errorCode);
    }

    @Override
    public boolean matches(Object item) {
        ApiException apiException = (ApiException) item;
        return apiException.getErrors().length == 1
                && apiException.getErrors()[0].getErrorCode() == errorCode
                && (!checkArgs || Arrays.equals(apiException.getErrors()[0].getArgs(), args));
    }
}
