package webserver.example;

import commons.lib.extra.server.http.handler.testLib.annotations.JsonField;

public class Body {

	@JsonField
    private final String toto;

    public Body(String toto) {
        this.toto = toto;
    }

    public String getToto() {
        return toto;
    }

    @Override
    public String toString() {
        return "Body{" +
                "toto='" + toto + '\'' +
                '}';
    }
}
