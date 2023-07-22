package webserver.handlers.web.auth;

import java.util.List;

public class AuthenticationResponse {
    private final String token;
    private final List<String> roles;

    public AuthenticationResponse(String token, List<String> roles) {
        this.token = token;
        this.roles = roles;
    }


    public String getToken() {
        return token;
    }

    public List<String> getRoles() {
        return roles;
    }

}
