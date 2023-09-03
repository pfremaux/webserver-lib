package webserver.handlers.web.auth;

import java.util.Objects;
import java.util.Set;

public record Account(String login, Set<String> roles) {

    public Account {
        Objects.requireNonNull(login);
        Objects.requireNonNull(roles);
        if (roles.isEmpty()) {
            throw new RuntimeException("Account without roles detected: " + login);
        }
    }

}
