package tool.config.internal;

import tool.config.Parameter;

import java.util.List;

public interface CliAction  extends Cli {
    List<Parameter> getAllParametersAllowed();
    List<Parameter> getRequiredParameters();
    List<Parameter> getOptionalParameters();
}
