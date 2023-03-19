package webserver.example;

import commons.lib.extra.server.http.handler.testLib.annotations.JsonField;
import webserver.example.Body;

import java.util.List;

public class TestArray {
    @JsonField
    private final List<String> s;
    @JsonField
    private final List<Double> n;
    @JsonField
    private final List<Body> o;

    public TestArray(List<String> s, List<Double> n, List<Body> o) {
        this.s = s;
        this.n = n;
        this.o = o;
    }

    public List<Body> getO() {
        return o;
    }

    public List<String> getS() {
        return s;
    }

    public List<Double> getN() {
        return n;
    }

    @Override
    public String toString() {
        return "TestArray{" +
                "s=" + s +
                ", n=" + n +
                '}';
    }
}
