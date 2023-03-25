package webserver.handlers.auth;

import java.util.HashMap;
import java.util.Map;

public class Token {
    private final Map<TokenField, String> tokenValues;

    public Token() {
        this.tokenValues = new HashMap<>();
    }

    public Token(Map<TokenField, String> tokenValues) {
        this.tokenValues = tokenValues;
    }


    public String put(TokenField tokenField, String value) {
        return tokenValues.put(tokenField, value);
    }

    public String get(TokenField tokenField) {
        return tokenValues.get(tokenField);
    }
}
