# Quickstart: Simple Role-Based Authentication

**Feature Branch**: `003-simple-auth`
**Date**: 2026-02-03

## Prerequisites

- Java 25
- Maven (via wrapper: `./mvnw`)
- Existing MoneyTrak application running on `003-simple-auth` branch

## New Dependencies

Add to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Configuration

### Default Users (application.yaml)

```yaml
moneytrak:
  security:
    users:
      - username: app-client
        password: "{bcrypt}$2a$10$<hash>"
        role: APP
      - username: backoffice
        password: "{bcrypt}$2a$10$<hash>"
        role: BACKOFFICE
      - username: admin
        password: "{bcrypt}$2a$10$<hash>"
        role: ADMIN
```

### Test Users (application-test.yaml)

Tests use simpler credentials. Use `{noop}` prefix for plaintext passwords in test profile for readability.

## Testing the Feature

### Run all tests
```bash
./mvnw test
```

### Manual testing with curl

```bash
# Unauthenticated (expect 401)
curl -i http://localhost:8080/v1/transactions

# APP role - read (expect 200)
curl -i -u app-client:password http://localhost:8080/v1/transactions

# APP role - write (expect 403)
curl -i -u app-client:password -X POST -H "Content-Type: application/json" \
  -d '{"description":"Test","amount":10.00,"currency":"USD","date":"2026-01-01T00:00:00Z"}' \
  http://localhost:8080/v1/transactions

# BACKOFFICE role - write (expect 201)
curl -i -u backoffice:password -X POST -H "Content-Type: application/json" \
  -d '{"description":"Test","amount":10.00,"currency":"USD","date":"2026-01-01T00:00:00Z"}' \
  http://localhost:8080/v1/transactions

# ADMIN role - actuator (expect 200)
curl -i -u admin:password http://localhost:8080/actuator/info

# Health check - no auth needed (expect 200)
curl -i http://localhost:8080/actuator/health
```

## New Source Files

```
src/main/java/dev/juanvaldivia/moneytrak/
├── security/
│   ├── SecurityConfig.java              # SecurityFilterChain beans
│   ├── SecurityProperties.java          # @ConfigurationProperties binding
│   ├── SecurityUserDetailsService.java  # Maps config users to UserDetails
│   ├── CustomAuthEntryPoint.java        # JSON 401 responses
│   └── CustomAccessDeniedHandler.java   # JSON 403 responses
```

## Key Design Decisions

1. **HTTP Basic Auth** — simplest mechanism, no token lifecycle management
2. **Config-defined users** — no database table, no user management endpoints
3. **URL-based authorization** — rules centralized in SecurityFilterChain, not scattered as annotations
4. **DelegatingPasswordEncoder** — BCrypt default with future algorithm migration support
5. **Existing tests updated** — `@WithMockUser(roles = "ADMIN")` added at class level
