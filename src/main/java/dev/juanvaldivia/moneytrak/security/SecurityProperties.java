package dev.juanvaldivia.moneytrak.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "moneytrak.security")
public record SecurityProperties(List<ConfigUser> users) {

    public record ConfigUser(String username, String password, String role) {}
}
