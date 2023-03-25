package webserver.handlers.auth;

public enum DefaultTokenFields implements TokenField {
    VERSION("version", 3),
    USER_ID("userId", 14),
    EXPIRATION_TIMESTAMP_MS("expirationMs", 14);

    protected final String key;
    protected final int length;

    DefaultTokenFields(String key, int length) {
        this.key = key;
        this.length = length;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public int getLength() {
        return length;
    }
}
