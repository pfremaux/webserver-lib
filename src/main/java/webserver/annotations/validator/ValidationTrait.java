package webserver.annotations.validator;

import webserver.handlers.web.ErrorReport;

public interface ValidationTrait {

    <T extends ValidationTrait>ErrorReport validate();

}
