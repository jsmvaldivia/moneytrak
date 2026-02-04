# Tasks: Simple Role-Based Authentication

**Input**: Design documents from `/specs/003-simple-auth/`
**Prerequisites**: plan.md (required), spec.md (required), research.md, data-model.md, contracts/

**Tests**: Included per constitution Principle III (TDD is NON-NEGOTIABLE). Red-Green-Refactor cycle enforced.

**Organization**: Tasks grouped by user story. Each story is independently testable after its phase completes.

**Current State**: The security infrastructure (5 source files, dependencies, configuration) is already implemented on this branch. However, 27 of 31 existing tests fail with 401 due to a Spring Security 7 / `@AutoConfigureMockMvc` compatibility issue. Tasks below account for this — already-completed work is marked `[x]`, remaining work is marked `[ ]`.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup

**Purpose**: Add Spring Security dependencies and create the security package structure

- [x] T001 Add `spring-boot-starter-security` dependency to `pom.xml`
- [x] T002 Add `spring-security-test` (scope: test) dependency to `pom.xml`
- [x] T003 Add test user configuration to `src/test/resources/application-test.yaml` with three users (app-client/APP, backoffice/BACKOFFICE, admin/ADMIN) using `{noop}` passwords

**Checkpoint**: Project compiles with security on classpath. Spring Security auto-config activates.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core security infrastructure that ALL user stories depend on. Must complete before any story work.

**CRITICAL**: No user story work can begin until this phase is complete.

- [x] T004 [P] Create `SecurityProperties` configuration record in `src/main/java/dev/juanvaldivia/moneytrak/security/SecurityProperties.java` — `@ConfigurationProperties(prefix = "moneytrak.security")` with nested `ConfigUser` record (username, password, role)
- [x] T005 [P] Create `CustomAuthEntryPoint` in `src/main/java/dev/juanvaldivia/moneytrak/security/CustomAuthEntryPoint.java` — implements `AuthenticationEntryPoint`, returns JSON 401 using `ErrorResponseDto`, logs failed auth attempts at WARN level per FR-014 (username, timestamp, IP)
- [x] T006 [P] Create `CustomAccessDeniedHandler` in `src/main/java/dev/juanvaldivia/moneytrak/security/CustomAccessDeniedHandler.java` — implements `AccessDeniedHandler`, returns JSON 403 using `ErrorResponseDto`
- [x] T007 Create `SecurityUserDetailsService` in `src/main/java/dev/juanvaldivia/moneytrak/security/SecurityUserDetailsService.java` — reads users from `SecurityProperties`, maps to `UserDetails` via `User.builder().username().password().roles()`, returns `InMemoryUserDetailsManager`
- [x] T008 Create `SecurityConfig` in `src/main/java/dev/juanvaldivia/moneytrak/security/SecurityConfig.java` — `@Configuration @EnableWebSecurity` with `SecurityFilterChain` bean: CSRF disabled, form login disabled, HTTP Basic enabled, custom entry point and access denied handler wired, authorization rules for health/actuator/v1 endpoints
- [x] T009 Add `moneytrak.security.users` section to `src/main/resources/application.yaml` with three users (app-client/APP, backoffice/BACKOFFICE, admin/ADMIN) using `{bcrypt}` hashed passwords
- [x] T010 Fix existing test classes so `@WithMockUser(roles = "ADMIN")` works with `@AutoConfigureMockMvc` under Spring Security 7 / Spring Boot 4. Currently 27 of 31 tests fail with 401 despite having `@WithMockUser` at class level. The `SecuritySmokeTest` works because it manually configures MockMvc with `springSecurity()`. Investigate whether `@AutoConfigureMockMvc` needs additional configuration or whether tests need to manually build MockMvc with `SecurityMockMvcConfigurers.springSecurity()`. Affected files:
  - `src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java`
  - `src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java`
  - `src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryIntegrationTest.java`
  - `src/test/java/dev/juanvaldivia/moneytrak/FinalIntegrationTest.java`
  - `src/test/java/dev/juanvaldivia/moneytrak/MoneytrakApplicationTests.java`
- [x] T011 Run `./mvnw test` — verify all existing tests pass with security enabled (zero regressions per SC-006). Expected: 31 tests pass, 0 failures.

**Checkpoint**: Foundation ready. Security is active, all existing tests pass, custom error handlers return JSON. User story implementation can now begin.

---

## Phase 3: User Story 1 — Protect All Endpoints with Authentication (Priority: P1) MVP

**Goal**: All endpoints require valid HTTP Basic credentials. Unauthenticated requests return 401 JSON response. Valid credentials grant access.

**Independent Test**: Send requests without credentials → 401. Send requests with valid credentials → success. Send requests with wrong password → 401.

### Tests for User Story 1

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T012 [US1] Write authentication tests in `src/test/java/dev/juanvaldivia/moneytrak/security/SecurityIntegrationTest.java` — `@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")` test class. Use `httpBasic()` request post-processor from `spring-security-test` for credential injection (not `@WithMockUser`). Tests:
  1. `unauthenticated_getTransactions_returns401` — `GET /v1/transactions` with no credentials → 401 + JSON body with `status: 401, error: "Unauthorized"`
  2. `invalidCredentials_getTransactions_returns401` — `GET /v1/transactions` with `httpBasic("wrong", "wrong")` → 401
  3. `validCredentials_getTransactions_returns200` — `GET /v1/transactions` with `httpBasic("admin", "admin")` → 200
  4. `malformedAuthHeader_getTransactions_returns401` — request with garbled Authorization header → 401
  5. `unauthenticated_postTransaction_returns401` — `POST /v1/transactions` with no credentials → 401
  6. `unauthenticated_getCategories_returns401` — `GET /v1/categories` with no credentials → 401

### Implementation for User Story 1

- [x] T013 [US1] Verify `SecurityConfig` authorization rules in `src/main/java/dev/juanvaldivia/moneytrak/security/SecurityConfig.java` — confirm that the existing config has: `/actuator/health` → `permitAll()`, `/actuator/**` → `hasRole("ADMIN")`, `GET /v1/**` → `hasAnyRole("APP", "BACKOFFICE", "ADMIN")`, `POST/PUT/DELETE /v1/**` → `hasAnyRole("BACKOFFICE", "ADMIN")`, all other → `authenticated()`. If any rule is missing or misordered, fix it.
- [x] T014 [US1] Run `./mvnw test` — verify T012 tests pass (all 6 authentication tests green) plus all existing tests still pass

**Checkpoint**: US1 complete. All endpoints are protected. Unauthenticated requests get 401 JSON. Valid credentials work. MVP achieved.

---

## Phase 4: User Story 2 — APP Role Read-Only Access (Priority: P2)

**Goal**: APP role can read all `/v1/**` resources but cannot create, update, or delete.

**Independent Test**: Authenticate as APP → GET endpoints succeed. POST/PUT/DELETE → 403 Forbidden JSON.

### Tests for User Story 2

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T015 [US2] Write APP role tests in `src/test/java/dev/juanvaldivia/moneytrak/security/SecurityIntegrationTest.java` — add tests using `httpBasic("app-client", "app-client")`:
  1. `appRole_getTransactions_returns200` — `GET /v1/transactions` → 200
  2. `appRole_getCategories_returns200` — `GET /v1/categories` → 200
  3. `appRole_getTransactionSummaryExpenses_returns200` — `GET /v1/transactions/summary/expenses` → 200
  4. `appRole_getTransactionSummaryIncome_returns200` — `GET /v1/transactions/summary/income` → 200
  5. `appRole_postTransaction_returns403` — `POST /v1/transactions` with valid JSON body → 403 + JSON body with `status: 403, error: "Forbidden"`
  6. `appRole_putTransaction_returns403` — `PUT /v1/transactions/{any-uuid}` → 403
  7. `appRole_deleteTransaction_returns403` — `DELETE /v1/transactions/{any-uuid}` → 403
  8. `appRole_postCategory_returns403` — `POST /v1/categories` → 403
  9. `appRole_putCategory_returns403` — `PUT /v1/categories/{any-uuid}` → 403
  10. `appRole_deleteCategory_returns403` — `DELETE /v1/categories/{any-uuid}` → 403

### Implementation for User Story 2

- [x] T016 [US2] Verify APP role authorization rules are correctly configured in `SecurityConfig` — the `requestMatchers(HttpMethod.GET, "/v1/**").hasAnyRole("APP", "BACKOFFICE", "ADMIN")` rule should already be in place from T008. Confirm POST/PUT/DELETE rules exclude APP. Fix if needed.
- [x] T017 [US2] Run `./mvnw test` — verify T015 tests pass (all 10 APP role tests green) plus all previous tests still pass

**Checkpoint**: US2 complete. APP users are read-only. Write attempts get 403.

---

## Phase 5: User Story 3 — BACKOFFICE Role Full CRUD (Priority: P2)

**Goal**: BACKOFFICE role can perform all CRUD operations on transactions and categories but cannot access actuator endpoints.

**Independent Test**: Authenticate as BACKOFFICE → all GET, POST, PUT, DELETE operations on /v1/** succeed. Actuator (beyond health) → 403.

### Tests for User Story 3

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T018 [US3] Write BACKOFFICE role tests in `src/test/java/dev/juanvaldivia/moneytrak/security/SecurityIntegrationTest.java` — add tests using `httpBasic("backoffice", "backoffice")`:
  1. `backofficeRole_postTransaction_returns201` — `POST /v1/transactions` with valid JSON body → 201 with Location header
  2. `backofficeRole_getTransactions_returns200` — `GET /v1/transactions` → 200
  3. `backofficeRole_putTransaction_returns200` — create a transaction, then `PUT /v1/transactions/{id}` with update body → 200
  4. `backofficeRole_deleteTransaction_returns204` — create a transaction, then `DELETE /v1/transactions/{id}` → 204
  5. `backofficeRole_postCategory_returns201` — `POST /v1/categories` with valid name → 201
  6. `backofficeRole_getCategories_returns200` — `GET /v1/categories` → 200
  7. `backofficeRole_getTransactionSummary_returns200` — `GET /v1/transactions/summary/expenses` → 200
  8. `backofficeRole_actuatorInfo_returns403` — `GET /actuator/info` → 403 (FR-007)

### Implementation for User Story 3

- [x] T019 [US3] Verify BACKOFFICE authorization rules are already in place in `SecurityConfig` from T008 — `hasAnyRole("BACKOFFICE", "ADMIN")` on POST/PUT/DELETE `/v1/**`. BACKOFFICE is explicitly included in the write rules. No changes expected; this is a verification task.
- [x] T020 [US3] Run `./mvnw test` — verify T018 tests pass (all 8 BACKOFFICE role tests green) plus all previous tests still pass

**Checkpoint**: US3 complete. BACKOFFICE users have full CRUD. Works alongside APP restrictions.

---

## Phase 6: User Story 4 — ADMIN Role Full System Access (Priority: P3)

**Goal**: ADMIN role gets all BACKOFFICE permissions plus access to actuator endpoints (beyond health) and H2 console.

**Independent Test**: Authenticate as ADMIN → all CRUD works + actuator/info accessible. BACKOFFICE and APP → actuator returns 403.

### Tests for User Story 4

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T021 [US4] Write ADMIN role and actuator tests in `src/test/java/dev/juanvaldivia/moneytrak/security/SecurityIntegrationTest.java` — add tests:
  1. `adminRole_actuatorInfo_returns200` — `GET /actuator/info` with `httpBasic("admin", "admin")` → 200
  2. `adminRole_postTransaction_returns201` — `POST /v1/transactions` with ADMIN credentials → 201 (verify ADMIN has BACKOFFICE-level access)
  3. `adminRole_getTransactions_returns200` — `GET /v1/transactions` with ADMIN credentials → 200
  4. `appRole_actuatorInfo_returns403` — `GET /actuator/info` with APP credentials → 403 (FR-005)

### Implementation for User Story 4

- [x] T022 [US4] Verify actuator authorization rules are in place in `SecurityConfig` — confirm `requestMatchers("/actuator/**").hasRole("ADMIN")` is ordered after `/actuator/health` permitAll. Also confirm H2 console rules: `requestMatchers("/h2-console/**").hasRole("ADMIN")` with frame options set to sameOrigin. Fix ordering if needed.
- [x] T023 [US4] Run `./mvnw test` — verify T021 tests pass (all 4 ADMIN/actuator tests green) plus all previous tests still pass

**Checkpoint**: US4 complete. ADMIN has full system access. BACKOFFICE and APP blocked from actuator.

---

## Phase 7: User Story 5 — Health Check Publicly Accessible (Priority: P3)

**Goal**: `/actuator/health` responds to unauthenticated requests. All other endpoints still require auth.

**Independent Test**: No credentials + health endpoint → 200. No credentials + any other endpoint → 401.

### Tests for User Story 5

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T024 [US5] Write health endpoint tests in `src/test/java/dev/juanvaldivia/moneytrak/security/SecurityIntegrationTest.java` — add tests:
  1. `healthEndpoint_noAuth_returns200` — `GET /actuator/health` with no credentials → 200
  2. `actuatorInfo_noAuth_returns401` — `GET /actuator/info` with no credentials → 401 (not public)
  3. `apiEndpoint_noAuth_returns401` — `GET /v1/transactions` with no credentials → 401 (regression check, cross-ref with T012)

### Implementation for User Story 5

- [x] T025 [US5] Verify health endpoint permitAll rule is in place in `SecurityConfig` — confirm `requestMatchers("/actuator/health").permitAll()` is first in the authorization rule chain. Fix if needed.
- [x] T026 [US5] Run `./mvnw test` — verify T024 tests pass (all 3 health endpoint tests green) plus all previous tests still pass

**Checkpoint**: US5 complete. Health endpoint is public. All other endpoints properly protected.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, failed auth logging verification, and documentation updates

- [x] T027 Write failed authentication logging test in `src/test/java/dev/juanvaldivia/moneytrak/security/SecurityIntegrationTest.java` — send invalid credentials and verify WARN-level log output contains `Failed authentication attempt:` with username and IP address fields (FR-014)
- [x] T028 Run full test suite `./mvnw clean test` — verify ALL tests pass (existing tests + all new security tests). Zero regressions per SC-006.
- [x] T029 Update `CLAUDE.md` to document the security feature — add/update sections covering: three roles (APP, BACKOFFICE, ADMIN), HTTP Basic auth, config-based users, role permission matrix, new `security/` package with 5 classes, test profile credentials, updated test count

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — already complete ✅
- **Foundational (Phase 2)**: Depends on Setup — T010 is the critical remaining task (fix test infrastructure)
- **US1 (Phase 3)**: Depends on Foundational (T010-T011) — MVP target
- **US2 (Phase 4)**: Depends on US1 (T012-T014) — needs verified SecurityConfig
- **US3 (Phase 5)**: Depends on US2 (T015-T017) — validates BACKOFFICE rules from US2
- **US4 (Phase 6)**: Depends on US1 (T012-T014) — adds actuator verification (independent of US2/US3)
- **US5 (Phase 7)**: Depends on US1 (T012-T014) — verifies health permitAll (independent of US2/US3/US4)
- **Polish (Phase 8)**: Depends on all user stories complete

### User Story Dependencies

- **US1 (P1)**: Foundation → blocks US2, US3, US4, US5
- **US2 (P2)**: US1 → implements/verifies role-based read-only rules
- **US3 (P2)**: US2 → verifies BACKOFFICE permissions (rules in place from US2)
- **US4 (P3)**: US1 → verifies actuator rules (independent of US2/US3)
- **US5 (P3)**: US1 → verifies health permitAll (independent of US2/US3/US4)

### Within Each User Story

- Tests MUST be written and FAIL before implementation
- Implementation verifies/refines SecurityConfig rules
- Test suite run confirms green + no regressions

### Parallel Opportunities

- **Phase 2**: T004-T006 already done in parallel (different files) ✅
- **Phase 4 + Phase 6**: US2 and US4 can be parallelized after US1 (different rule sets: /v1/ roles vs actuator roles)
- **Phase 6 + Phase 7**: US4 and US5 can be parallelized (different concerns: actuator auth vs health public)

---

## Parallel Example: Post-US1 Parallel Execution

```bash
# After US1 is complete, these can run in parallel:

# Track A: Role-based access (US2 → US3)
Task: "Write APP role tests in SecurityIntegrationTest.java"
Task: "Verify APP role rules in SecurityConfig"

# Track B: System access (US4 + US5)
Task: "Write ADMIN actuator tests in SecurityIntegrationTest.java"
Task: "Write health endpoint tests in SecurityIntegrationTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 2: Fix test infrastructure (T010-T011) — critical blocker
2. Complete Phase 3: US1 — Write auth tests, verify config, run suite
3. **STOP and VALIDATE**: `./mvnw test` green, verify 401/200 behavior
4. This is a deployable increment — all endpoints are now protected

### Incremental Delivery

1. Fix tests + US1 → Authentication works → Deploy/Demo (MVP!)
2. US2 → APP role is read-only → Deploy/Demo
3. US3 → BACKOFFICE has full CRUD → Deploy/Demo
4. US4 → ADMIN has actuator access → Deploy/Demo
5. US5 → Health endpoint public → Deploy/Demo
6. Polish → Logging verified, docs updated → Final Deploy

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Constitution Principle III requires TDD — all test tasks precede implementation
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Phase 1 (T001-T003) and most of Phase 2 (T004-T009) are already complete
- **Critical remaining work**: T010 (fix test infrastructure) is the single blocker
- Total: 29 tasks across 8 phases (9 already complete, 20 remaining)
- New security tests expected: ~31 tests across all user stories
- Existing tests to pass: 31 tests (currently 27 failing due to T010)
