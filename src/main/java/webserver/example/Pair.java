package webserver.example;

import commons.lib.extra.server.http.handler.testLib.annotations.JsonField;

public class Pair {
	@JsonField
    private final int a;
	@JsonField
    private final int b;

    public Pair(int a, int b) {
        this.a = a;
        this.b = b;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "a=" + a +
                ", b=" + b +
                '}';
    }
}
