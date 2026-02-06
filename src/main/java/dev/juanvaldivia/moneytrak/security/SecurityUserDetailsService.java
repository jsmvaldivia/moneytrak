package dev.juanvaldivia.moneytrak.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.stereotype.Component;

@Component
public class SecurityUserDetailsService {

    private final InMemoryUserDetailsManager userDetailsManager;

    public SecurityUserDetailsService(SecurityProperties securityProperties) {
        UserDetails[] users = securityProperties.users().stream()
                .map(configUser -> User.builder()
                        .username(configUser.username())
                        .password(configUser.password())
                        .roles(configUser.role().toUpperCase())
                        .build())
                .toArray(UserDetails[]::new);

        this.userDetailsManager = new InMemoryUserDetailsManager(users);
    }

    public InMemoryUserDetailsManager getUserDetailsManager() {
        return userDetailsManager;
    }
}
