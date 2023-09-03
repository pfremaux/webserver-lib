package webserver.handlers.web.auth;

import java.util.Objects;
import java.util.Set;

public final class AuthenticationResponse {
    private final String token;
    private final Set<String> roles;

    public AuthenticationResponse(String token, Set<String> roles) {
        this.token = token;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AuthenticationResponse) obj;
        return Objects.equals(this.token, that.token) &&
                Objects.equals(this.roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, roles);
    }

    @Override
    public String toString() {
        return "AuthenticationResponse[" +
                "token=" + token + ", " +
                "roles=" + roles + ']';
    }


}
