package webserver.handlers.web;

public enum BaseError implements ErrorReport {

    METHOD_NOT_ALLOWED("Method not allowed.", "1"),
    MISSING_BODY("Missing request body.", "2");

    private final String message;
    private final String errorCode;

    BaseError(String message, String errorCode) {
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
