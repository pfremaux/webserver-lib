package webserver.example.endpointslist;

import webserver.annotations.JsonField;

import java.util.List;

public class EndpointList {
    @JsonField
    private final List<EndpointData> endpoints;

    public EndpointList(List<EndpointData> endpoints) {
        this.endpoints = endpoints;
    }

    public List<EndpointData> getEndpoints() {
        return endpoints;
    }
}
