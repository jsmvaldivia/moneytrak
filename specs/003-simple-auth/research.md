# Research: Simple Role-Based Authentication

**Feature Branch**: `003-simple-auth`
**Date**: 2026-02-03

## R-001: Spring Security Dependency

**Decision**: Use `spring-boot-starter-security` with no version specified. Spring Boot 4.0.1 manages Spring Security 7.0.2 automatically via the BOM. Add `spring-security-test` for test support.

**Rationale**: Letting the Boot BOM manage the version ensures compatibility across all Spring modules. No manual overrides needed.

**Alternatives considered**:
- Manual `spring-security-web` + `spring-security-config` dependencies — unnecessary with Boot starter.

## R-002: HTTP Basic Authentication Configuration

**Decision**: Define a `SecurityFilterChain` bean using lambda DSL with `.httpBasic(withDefaults())`. Disable CSRF (stateless REST API). Disable form login.

**Rationale**: HTTP Basic is the simplest standard auth mechanism for server-to-server API communication. Lambda DSL is the only option in Spring Security 7 (non-lambda DSL and `and()` chaining removed).

**Alternatives considered**:
- JWT/OAuth2 — adds significant complexity (token lifecycle, signing keys, refresh flows). Spec explicitly requests simplicity.
- Session-based auth — unnecessary for API-only application with no browser clients.

**Key migration notes for Spring Security 7**:
- `and()` method removed — must use lambda DSL
- `authorizeRequests()` removed — use `authorizeHttpRequests()`
- `AntPathRequestMatcher`/`MvcRequestMatcher` removed — use `PathPatternRequestMatcher` (or `requestMatchers(String...)` convenience method)

## R-003: In-Memory User Configuration with Roles

**Decision**: Use `InMemoryUserDetailsManager` with users defined via `@ConfigurationProperties` from `application.yaml`. Map YAML-defined users to `UserDetails` objects with `User.builder()`.

**Rationale**: Satisfies FR-012 (configurable, not hardcoded). `InMemoryUserDetailsManager` is appropriate for small user sets (spec says under 10). `@ConfigurationProperties` provides type-safe binding with IDE autocomplete via the configuration processor already in the project.

**Alternatives considered**:
- `spring.security.user.name`/`password` — only supports a single user, not sufficient for three roles.
- Database-backed `UserDetailsService` — overkill for this use case.
- Direct bean definition with hardcoded users — violates FR-012.

**Critical pitfall**: `.roles("APP")` auto-prepends `ROLE_` → authority becomes `ROLE_APP`. Never pass `roles("ROLE_APP")` which would produce `ROLE_ROLE_APP`.

## R-004: Custom JSON Error Responses

**Decision**: Implement custom `AuthenticationEntryPoint` (401) and `AccessDeniedHandler` (403) that return JSON in the existing `ErrorResponseDto` format `{status, error, message, details[]}`.

**Rationale**: FR-013 requires auth errors to match existing format. Default Spring Security entry point returns HTML/plain text. Custom handlers ensure consistent JSON API responses.

**Wiring**: Set entry point on both `.exceptionHandling()` and `.httpBasic()` configurers. The httpBasic entry point overrides the global one for Basic auth flows.

## R-005: URL-Based vs Method-Level Security

**Decision**: Use URL-based security via `requestMatchers().hasRole()` in `SecurityFilterChain`. No `@PreAuthorize` annotations.

**Rationale**: All authorization decisions map to role + HTTP method + URL pattern. No method-parameter inspection needed. Centralizing rules in SecurityFilterChain keeps them auditable in one place. The role permission matrix translates directly to:
- `GET /v1/**` → APP, BACKOFFICE, ADMIN
- `POST|PUT|DELETE /v1/**` → BACKOFFICE, ADMIN
- `/actuator/**` (except health) → ADMIN only

**Alternatives considered**:
- `@PreAuthorize` annotations — adds complexity and scatters security rules across controllers. Can be added later if needed.

## R-006: Test Support

**Decision**: Use `@SpringBootTest` + `@AutoConfigureMockMvc` with `httpBasic("user", "password")` request post-processor. Use `@WithMockUser` for simpler role-based tests. Existing 40 tests need credential updates.

**Rationale**: All existing test utilities (`@WithMockUser`, `@WithAnonymousUser`, `httpBasic()`) remain available in Spring Security 7. Existing tests will fail with 401 after security is enabled.

**Migration strategy for existing tests**: Add `@WithMockUser(roles = "ADMIN")` at class level to existing test classes. This is the least-disruptive approach since it can be applied without modifying individual test methods.

## R-007: Actuator Security

**Decision**: Use a single `SecurityFilterChain` with `EndpointRequest` matchers for actuator rules, ordered before application rules. Health endpoint is `permitAll()`, other actuator endpoints require ADMIN role.

**Rationale**: The rule set is simple enough for a single chain. Using `EndpointRequest.to("health")` and `EndpointRequest.toAnyEndpoint()` provides clean actuator matching without hardcoding paths.

**Alternatives considered**:
- Separate `SecurityFilterChain` with `@Order` — viable but adds complexity for only two actuator rules.
- Separate management port — operational decision beyond this spec's scope.

## R-008: Password Encoding

**Decision**: Use `DelegatingPasswordEncoder` via `PasswordEncoderFactories.createDelegatingPasswordEncoder()`. Store passwords in YAML with `{bcrypt}` prefix.

**Rationale**: `DelegatingPasswordEncoder` is explicitly recommended by Spring Security. It supports multiple algorithms simultaneously and enables future migration without re-hashing. BCrypt with default strength 10 is the standard.

**Alternatives considered**:
- `BCryptPasswordEncoder` directly — works but lacks future algorithm flexibility.
- `{noop}` prefix for plaintext — development/testing only.
- Argon2 — stronger but more resource-intensive; unnecessary for this use case.
