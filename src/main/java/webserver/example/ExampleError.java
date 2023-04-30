package webserver.example;

import webserver.handlers.web.ErrorReport;

public enum ExampleError implements ErrorReport {

    BAD_TOTO("Attribute toto should start with 'toto'.", "1");

    private final String message;
    private final String errorCode;

    ExampleError(String message, String errorCode) {
        this.message = message;
        this.errorCode = errorCode;
    }

    @Override
    public String getErrorMessage() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
