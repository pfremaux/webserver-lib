package webserver.handlers.auth;

import webserver.annotations.JsonField;

public class AuthBody {

    @JsonField
    private final String login;
    @JsonField
    private final String pass;

    public AuthBody(String login, String pass) {
        this.login = login;
        this.pass = pass;
    }

    public String getLogin() {
        return login;
    }

    public String getPass() {
        return pass;
    }
}
