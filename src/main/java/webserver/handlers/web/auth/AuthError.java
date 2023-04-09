package webserver.handlers.web.auth;

import webserver.handlers.web.ErrorReport;

public enum AuthError implements ErrorReport {

    BAD_CREDENTIALS("Wrong credentials.", "1");

    private final String message;
    private final String errorCode;

    AuthError(String message, String errorCode) {
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
