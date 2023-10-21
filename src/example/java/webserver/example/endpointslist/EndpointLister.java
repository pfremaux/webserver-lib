package webserver.example.endpointslist;

import tools.Singletons;
import webserver.annotations.Endpoint;

import java.util.Map;

public class EndpointLister {
    @Endpoint(method = "GET", path = "/self-describe/json")
    public EndpointList listEndpoints(Map<String, Object> header) {
        return Singletons.get(EndpointList.class);
    }
}
