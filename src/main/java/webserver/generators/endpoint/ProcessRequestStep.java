package webserver.generators.endpoint;

import webserver.handlers.web.ErrorReport;

record ProcessRequestStep(
        Exception exception,
        ErrorReport errorReport,
        Object result,
        boolean leave) {
    static ProcessRequestStep ALL_GOOD = new ProcessRequestStep(null, null, null, false);
    ProcessRequestStep(Exception e) {
        this(e, null, null, true);
    }

    ProcessRequestStep(ErrorReport errorReport) {
        this(null, errorReport, null, true);
    }

    ProcessRequestStep(boolean leave) {
        this(null, null, null, leave);
    }

    ProcessRequestStep() {
        this(null, null, null, false);
    }

    ProcessRequestStep withResult(Object obj) {
        return new ProcessRequestStep(exception(), errorReport(), obj, leave());
    }

}