package webserver.validator;

import webserver.handlers.web.ErrorReport;

public interface ValidationTrait {

    <T extends ValidationTrait>ErrorReport validate();

}
