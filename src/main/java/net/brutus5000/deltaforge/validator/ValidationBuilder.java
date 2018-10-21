package net.brutus5000.deltaforge.validator;

import lombok.extern.slf4j.Slf4j;
import net.brutus5000.deltaforge.error.ApiException;
import net.brutus5000.deltaforge.error.Error;
import net.brutus5000.deltaforge.error.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
public class ValidationBuilder {
    private final List<Error> errors = new ArrayList<>();

    public static <T> boolean changed(T a, T b) {
        return !Objects.equals(a, b);
    }

    private void add(ErrorCode errorCode, Object... args) {
        log.debug("ErrorCode ''{}'' was added to list. Arguments: {}", errorCode, args);
        errors.add(new Error(errorCode, args));
    }

    public <T> ValidationBuilder apply(T value, BiFunction<ValidationBuilder, T, ValidationBuilder> assertFunction) {
        assertFunction.apply(this, value);
        return this;
    }

    public <T> ValidationBuilder conditionalAssert(BooleanSupplier conditionalExpression, Predicate<T> predicate, T value, ErrorCode errorCode, Object... args) {
        if (conditionalExpression.getAsBoolean() && !predicate.test(value)) {
            add(errorCode, args);
        }
        return this;
    }

    public ValidationBuilder conditionalAssertNotBlank(BooleanSupplier conditionalExpression, String value, String fieldName) {
        Predicate<String> notNull = Objects::nonNull;
        Predicate<String> notEmpty = s -> !"".equals(s);
        conditionalAssert(conditionalExpression, notNull, value, ErrorCode.PROPERTY_IS_NULL, fieldName);
        conditionalAssert(conditionalExpression, notEmpty, value, ErrorCode.STRING_IS_EMPTY, fieldName);
        return this;
    }

    public ValidationBuilder assertNotBlank(String value, String fieldName) {
        conditionalAssertNotBlank(() -> true, value, fieldName);
        return this;
    }

    public ValidationBuilder conditionalAssertNotNull(BooleanSupplier conditionalExpression, Object value, String fieldName) {
        Predicate<Object> notNull = Objects::nonNull;
        conditionalAssert(conditionalExpression, notNull, value, ErrorCode.PROPERTY_IS_NULL, fieldName);
        return this;
    }

    public ValidationBuilder assertNotNull(Object value, String fieldName) {
        conditionalAssertNotNull(() -> true, value, fieldName);
        return this;
    }

    public <T> ValidationBuilder conditionalAssertNotExists(BooleanSupplier conditionalExpression, Function<T, Optional<?>> query, T criteria, ErrorCode errorCode, Object... args) {
        Predicate<T> exists = t -> !query.apply(t).isPresent();
        conditionalAssert(conditionalExpression, exists, criteria, errorCode, args);
        return this;
    }

    public <T> ValidationBuilder assertNotExists(Function<T, Optional<?>> query, T criteria, ErrorCode errorCode, Object... args) {
        conditionalAssertNotExists(() -> true, query, criteria, errorCode, args);
        return this;
    }

    public <T> ValidationBuilder conditionalAssertEquals(BooleanSupplier conditionalExpression, T reference, T value, ErrorCode errorCode, Object... args) {
        Predicate<T> equals = t -> Objects.equals(t, reference);
        conditionalAssert(conditionalExpression, equals, value, errorCode, args);
        return this;
    }

    public <T> ValidationBuilder assertEquals(T reference, T value, ErrorCode errorCode, Object... args) {
        conditionalAssertEquals(() -> true, reference, value, errorCode, args);
        return this;
    }

    public <T> ValidationBuilder assertUnchanged(T reference, T value, ErrorCode errorCode, Object... args) {
        assertEquals(reference, value, errorCode, args);
        return this;
    }

    public static BooleanSupplier whenChanged(Object current, Object preUpdate) {
        return () -> !Objects.equals(current, preUpdate);
    }

    public static BooleanSupplier whenNotNull(Object value) {
        return () -> Objects.nonNull(value);
    }

    public static BooleanSupplier and(BooleanSupplier a, BooleanSupplier b) {
        return () -> a.getAsBoolean() && b.getAsBoolean();
    }

    public void validate() throws ApiException {
        if (!errors.isEmpty()) {
            log.trace("ValidationBuilder contains {} errors", errors.size());
            Error[] errorArray = errors.toArray(new Error[0]);
            throw ApiException.of(errorArray);
        }

        log.trace("ValidationBuilder is empty");
    }
}
