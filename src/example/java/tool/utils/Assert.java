package tool.utils;

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class Assert {

    public static void main(String[] args) {
        requireNotEmpty(List.of(""), "test");
    }

    public static void requireNotNull(Collection<?> collection, String errorMessage) {
        validate(collection, Objects::nonNull, errorMessage);
    }


    public static <T> void requireEqual(T i1, T i2, String errorMessage) {
        validate(i1, i2, Object::equals, errorMessage);
    }


    public static void requireNotEmpty(Collection<?> collection, String errorMessage) {
        requireNotNull(collection, "Collection mustn't be null. " + errorMessage);
        validateNot(collection, Collection::isEmpty, errorMessage);
    }

    public static void requireExists(Path path, String errorMessage) {
        validate(path, p -> p.toFile().exists(), errorMessage);
    }

    public static <T> T require(Optional<T> input, String errorMessage) {
        return validate(input, Optional::isPresent, errorMessage).get();
    }

    public static void requireNotExists(Path path, String errorMessage) {
        validateNot(path, p -> p.toFile().exists(), errorMessage);
    }

    public static void requireGreaterOrEqual(int testingValue, int reference, String errorMessage) {
        validate(testingValue, reference, (a, ref) -> a >= ref, errorMessage);
    }

    private static <T> void validateNot(T i1, Predicate<T> validator, String errorMessage) {
        validate(i1, validator.negate(), errorMessage);
    }

    private static <T> T validate(T i1, Predicate<T> validator, String errorMessage) {
        if (!validator.test(i1)) {
            throw new IllegalStateException(errorMessage);
        }
        return i1;
    }

    private static <T> void validate(T i1, T i2, BiPredicate<T, T> validator, String errorMessage) {
        if (!validator.test(i1, i2)) {
            throw new IllegalStateException(errorMessage);
        }
    }


}
