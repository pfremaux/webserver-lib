package webserver.handlers.web.auth;


import java.util.*;

public class TokenStructure {
    private final Map<String, TokenField> keys = new HashMap<>();
    private final List<TokenField> tokenFields = new ArrayList<>();
    private final Integer size;

    public TokenStructure(TokenField... tokenFields) {
        for (TokenField tokenField : tokenFields) {
            register(tokenField);
        }
        this.size = calculateSize();
    }

    private int calculateSize() {
        int sum = 0;
        for (TokenField value : keys.values()) {
            sum += value.getLength();
        }
        return sum;
    }

    public char[] prepareCleanToken() {
        char[] dest = new char[size];
        Arrays.fill(dest, '=');
        return dest;
    }

    public String getFormattedTokenInClear(Token token) {
        final StringBuilder builder = new StringBuilder();
        for (TokenField tokenField : tokenFields) {
            String s = token.get(tokenField);
            if (s == null) {
                System.err.println("Field " + tokenField + " can't be null.");
            }
            builder.append(formatFieldForToken(s, tokenField.getLength()));
        }
        return builder.toString();
    }

    public Token parseAndStoreTokenDeciphered(String decryptedToken) {
        final Map<TokenField, String> tokenValues = new HashMap<>();

        int index = 0;
        for (TokenField tokenField : tokenFields) {
            final int length = tokenField.getLength();
            final String substring = decryptedToken.substring(index, index + length).replaceAll("=", "");
            tokenValues.put(tokenField, substring);
            index += length;
        }
        return new Token(tokenValues);
    }

    private static String formatFieldForToken(String s, int length) {
        char[] dest = new char[length];
        Arrays.fill(dest, '=');
        char[] toCharArray = s.toCharArray();
        System.arraycopy(toCharArray, 0, dest, 0, toCharArray.length);
        return new String(dest);
    }

    private void register(TokenField tokenField) {
        keys.put(tokenField.getKey(), tokenField);
        tokenFields.add(tokenField);
    }

    public List<TokenField> getTokenFields() {
        return tokenFields;
    }

    public Map<String, TokenField> getKeys() {
        return keys;
    }
}
