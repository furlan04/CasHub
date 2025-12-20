package it.unimib.CasHub.model;

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
public abstract class Result<T> {
    private Result() {}

    public boolean isSuccess() {
        return this instanceof Success;
    }

    public static final class Success<T> extends Result<T> {
        private final T data;
        public Success(T data) {
            this.data = data;
        }
        public T getData() {
            return data;
        }
    }

    public static final class Error<T> extends Result<T> {
        private final String message;
        public Error(String message) {
            this.message = message;
        }
        public String getMessage() {
            return message;
        }
    }
}
