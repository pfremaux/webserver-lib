package webserver.example;


import webserver.annotations.JsonField;

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
