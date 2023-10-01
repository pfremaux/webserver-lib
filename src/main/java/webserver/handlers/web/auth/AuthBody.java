package webserver.handlers.web.auth;

import webserver.annotations.JsonField;
import webserver.annotations.JsonParameter;

public class AuthBody {

    @JsonField
    private final String login;
    @JsonField
    private final String pass;

    public AuthBody(
            @JsonParameter(name = "login") String login,
            @JsonParameter(name = "pass") String pass) {
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
