package webserver.example;

import commons.lib.extra.server.http.handler.testLib.annotations.JsonField;

public class SumResult {
	@JsonField
    private final int result;

    public SumResult(int result) {
        this.result = result;
    }

    public int getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "SumResult{" +
                "result=" + result +
                '}';
    }
}
