package webserver.handlers.web.auth;

import webserver.handlers.web.ErrorReport;

public enum AuthError implements ErrorReport {

    BAD_CREDENTIALS("Wrong credentials.", "1"),
    ALGORITHM_ENCRYPTION("Algorithm encryption error. Mostly a server issue.", "2");

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
