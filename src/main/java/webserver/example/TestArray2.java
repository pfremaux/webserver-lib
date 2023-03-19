package webserver.example;

import commons.lib.extra.server.http.handler.testLib.annotations.JsonField;

import java.util.List;

public class TestArray2 {
	@JsonField
    private final List<String> s;
	@JsonField
    private final List<Double> n;


    public TestArray2(List<String> s, List<Double> n) {
        this.s = s;
        this.n = n;
    }

    public List<Double> getN() {
        return n;
    }

    public List<String> getS() {
        return s;
    }

    @Override
    public String toString() {
        return "TestArray2{" +
                "s=" + s +
                '}';
    }
}
