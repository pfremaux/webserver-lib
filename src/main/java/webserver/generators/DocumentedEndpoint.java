package webserver.generators;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

public class DocumentedEndpoint {
    private String javaMethodName;
    private String httpMethod;
    private String path;
    private String description;
    private String bodyExample;
    private Class<?> bodyType;
    private String responseExample;
    private String role;
    private Map<String, String> parameters = new HashMap<>();
    private Parameter returnType = null;
    private boolean hasForm;

    public DocumentedEndpoint() {

    }

    public DocumentedEndpoint(String role, String javaMethodName, String httpMethod, String path, String description, String bodyExample, String responseExample, Map<String, String> parameters, Parameter returnType) {
        this.javaMethodName = javaMethodName;
        this.httpMethod = httpMethod;
        this.path = path;
        this.role = role;
        this.description = description;
        this.bodyExample = bodyExample;
        this.responseExample = responseExample;
        this.parameters = parameters;
        this.returnType = returnType;
    }


    public String getHttpMethod() {
        return httpMethod;
    }

    public String getPath() {
        return path;
    }

    public String getDescription() {
        return description;
    }

    public String getResponseExample() {
        return responseExample;
    }

    public String getBodyExample() {
        return bodyExample;
    }

    public void setHttpMethod(String method) {
        this.httpMethod = method;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBodyExample(String bodyExample) {
        this.bodyExample = bodyExample;
    }

    public void setResponseExample(String responseExample) {
        this.responseExample = responseExample;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Parameter getReturnType() {
        return returnType;
    }

    public void setReturnType(Parameter returnType) {
        this.returnType = returnType;
    }

    public String getJavaMethodName() {
        return javaMethodName;
    }

    public void setJavaMethodName(String javaMethodName) {
        this.javaMethodName = javaMethodName;
    }

    public Class<?> getBodyType() {
        return bodyType;
    }

    public void setBodyType(Class<?> bodyType) {
        this.bodyType = bodyType;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRole() {
        return role;
    }

    public boolean isHasForm() {
        return hasForm;
    }

    public void setHasForm(boolean hasForm) {
        this.hasForm = hasForm;
    }
}
