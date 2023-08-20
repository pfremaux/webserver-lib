package webserver.validator;

import webserver.handlers.web.ErrorReport;

import java.util.Optional;

public interface ValidationTrait {

    <T extends ValidationTrait> Optional<ErrorReport> validate();

}
