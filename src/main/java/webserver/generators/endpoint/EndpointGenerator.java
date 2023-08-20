package webserver.generators.endpoint;

import com.sun.net.httpserver.HttpHandler;
import tools.JsonMapper;
import tools.MdDoc;
import webserver.annotations.Endpoint;
import webserver.annotations.Form;
import webserver.annotations.Role;
import webserver.generators.DocumentedEndpoint;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static webserver.generators.endpoint.AbstractEndpointHandler.processAbstractEndpoint;

public class EndpointGenerator {
    private EndpointGenerator() {
    }

    public static Map<String, HttpHandler> loadHttpHandlers(List<Object> instancesEndpointsToRegister, List<Consumer<DocumentedEndpoint>> addons) {
        final Map<String, HttpHandler> handlers = new HashMap<>();
        for (Object instanceWithEndpointsToProcess : instancesEndpointsToRegister) {
            extractEndpointsFromClasses(instanceWithEndpointsToProcess, addons, handlers);
        }
        return handlers;
    }

    private static Map<String, HttpHandler> extractEndpointsFromClasses(Object instanceWithEndpointsToRegister, List<Consumer<DocumentedEndpoint>> addons, Map<String, HttpHandler> handlers) {
        final Class<?> classTypeOfInstanceToProcess = instanceWithEndpointsToRegister.getClass();
        //final StringBuilder formGeneratorBuilder = new StringBuilder();// TODO PFR work on it once I'm on JS generation
        // Look for all endpoint and process all methods annotated @Endpoint
        for (Method declaredMethod : classTypeOfInstanceToProcess.getDeclaredMethods()) {
            final Endpoint declaredAnnotation = declaredMethod.getDeclaredAnnotation(Endpoint.class);
            if (declaredAnnotation == null) {
                continue;
            }
            final Role requiredRole = declaredMethod.getDeclaredAnnotation(Role.class);
            // Gets the body instance
            final Optional<Parameter> bodyParameter = Stream.of(declaredMethod.getParameters())
                    .filter((Parameter p) -> !p.getType().equals(Map.class))
                    .findFirst();

            final String methodName = declaredAnnotation.method();
            final String path = declaredAnnotation.path();
            if (handlers.containsKey(path)) {
                throw new IllegalArgumentException("Duplicate path: '%s' even if they have different HTTP method.".formatted(path));
            }

            final HttpHandler handler = mutableInputOutputContext -> processAbstractEndpoint(instanceWithEndpointsToRegister, declaredMethod, requiredRole, bodyParameter, methodName, mutableInputOutputContext);
            handlers.put(path, handler);

            registerDocumentation(addons, declaredMethod, requiredRole, bodyParameter, methodName, path);
        }
        return handlers;
    }

    private static void registerDocumentation(List<Consumer<DocumentedEndpoint>> addons, Method declaredMethod, Role requiredRole, Optional<Parameter> bodyParameter, String methodName, String path) {
        final DocumentedEndpoint documentedEndpoint = new DocumentedEndpoint();
        documentedEndpoint.setJavaMethodName(declaredMethod.getName());
        documentedEndpoint.setHttpMethod(methodName);
        documentedEndpoint.setPath(path);
        documentedEndpoint.setRole(requiredRole == null ? null : requiredRole.value());

        try {
            if (bodyParameter.isPresent()) {
                final Class<?> bodyParameterType = bodyParameter.get().getType();
                documentedEndpoint.setBodyExample(JsonMapper.objectToJsonExample(bodyParameterType).toString());
                documentedEndpoint.setBodyType(bodyParameterType);
                final Map<String, String> nameToTypeParameters = JsonMapper.objectToMapDescriptor(bodyParameterType);
                documentedEndpoint.setParameters(nameToTypeParameters);
                final Optional<Form> formAnnotation = Optional.ofNullable(bodyParameterType.getAnnotation(Form.class));
                documentedEndpoint.setHasForm(formAnnotation.isPresent());
            }
            documentedEndpoint.setResponseExample(JsonMapper.objectToJsonExample(declaredMethod.getReturnType()).toString());
        } catch (ClassNotFoundException e1) {
            throw new IllegalStateException("Endpoint creation failed.", e1);
        }

        final MdDoc declaredDoc = declaredMethod.getDeclaredAnnotation(MdDoc.class);
        if (declaredDoc != null) {
            documentedEndpoint.setDescription(Objects.requireNonNullElse(declaredDoc.description(), ""));
        }
        for (Consumer<DocumentedEndpoint> addon : addons) {
            addon.accept(documentedEndpoint);
        }
    }


}



