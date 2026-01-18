# Tasks: Expense Tracking API

**Input**: Design documents from `/specs/001-expense-tracking/`
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, contracts/openapi.yaml ✅

**Tests**: Included - Constitution Gate 3 (TDD) is NON-NEGOTIABLE

**Organization**: Tasks grouped by user story to enable independent implementation and testing

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story (US1, US2, US3, US4)
- Exact file paths included

## Path Conventions

Standard Spring Boot Maven project structure:
- Source: `src/main/java/dev/juanvaldivia/moneytrak/`
- Tests: `src/test/java/dev/juanvaldivia/moneytrak/`
- Resources: `src/main/resources/`, `src/test/resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and dependencies

- [ ] T001 Add spring-boot-starter-data-jpa dependency to pom.xml
- [ ] T002 Add com.h2database:h2 dependency (runtime scope) to pom.xml
- [ ] T003 [P] Create application.yaml in src/main/resources/ with H2 file database configuration
- [ ] T004 [P] Create application-test.yaml in src/test/resources/ with H2 in-memory configuration
- [ ] T005 [P] Add data/ directory to .gitignore for H2 database files

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T006 Create Expense entity (domain model) in src/main/java/dev/juanvaldivia/moneytrak/expenses/Expense.java
- [ ] T007 Create ExpenseRepository interface in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseRepository.java
- [ ] T008 [P] Create ExpenseCreationDto in src/main/java/dev/juanvaldivia/moneytrak/expenses/dto/ExpenseCreationDto.java
- [ ] T009 [P] Create ExpenseUpdateDto in src/main/java/dev/juanvaldivia/moneytrak/expenses/dto/ExpenseUpdateDto.java
- [ ] T010 [P] Create ExpenseDto in src/main/java/dev/juanvaldivia/moneytrak/expenses/dto/ExpenseDto.java
- [ ] T011 [P] Create ErrorResponseDto in src/main/java/dev/juanvaldivia/moneytrak/expenses/dto/ErrorResponseDto.java
- [ ] T012 [P] Create FieldErrorDto in src/main/java/dev/juanvaldivia/moneytrak/expenses/dto/FieldErrorDto.java
- [ ] T013 Create ExpenseMapper component in src/main/java/dev/juanvaldivia/moneytrak/expenses/mapper/ExpenseMapper.java
- [ ] T014 [P] Create @ValidCurrency annotation in src/main/java/dev/juanvaldivia/moneytrak/expenses/validation/ValidCurrency.java
- [ ] T015 [P] Create CurrencyValidator in src/main/java/dev/juanvaldivia/moneytrak/expenses/validation/CurrencyValidator.java
- [ ] T016 [P] Create NotFoundException in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/NotFoundException.java
- [ ] T017 [P] Create ConflictException in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/ConflictException.java
- [ ] T018 Create GlobalExceptionHandler (@RestControllerAdvice) in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/GlobalExceptionHandler.java
- [ ] T019 Create ExpenseService interface in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseService.java
- [ ] T020 Create LocalExpenseService implementation in src/main/java/dev/juanvaldivia/moneytrak/expenses/LocalExpenseService.java

**Checkpoint**: Foundation ready - user story implementation can now begin

---

## Phase 3: User Story 1 - Record Single Expense (Priority: P1) 🎯 MVP

**Goal**: Allow users to record a single expense with description, amount, currency, and date

**Independent Test**: Submit expense via POST /v1/expenses, verify 201 response with ID and version=1

### Tests for User Story 1 (TDD: Write FIRST, ensure FAIL)

- [ ] T021 [P] [US1] Create ExpenseRepositoryTest (@DataJpaTest) in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseRepositoryTest.java
- [ ] T022 [P] [US1] Create ExpenseMapperTest (unit) in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseMapperTest.java
- [ ] T023 [P] [US1] Create CurrencyValidatorTest (unit) in src/test/java/dev/juanvaldivia/moneytrak/expenses/validation/CurrencyValidatorTest.java
- [ ] T024 [US1] Create ExpenseServiceTest (unit with mocked repo) in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseServiceTest.java
- [ ] T025 [US1] Create ExpenseControllerTest (@WebMvcTest) for POST /v1/expenses in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T026 [US1] Create ExpenseControllerIntegrationTest (@SpringBootTest) for POST in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerIntegrationTest.java

**TDD Checkpoint**: All tests written and FAILING - ready for implementation

### Implementation for User Story 1

- [ ] T027 [US1] Implement Expense.create() factory method per data-model.md in src/main/java/dev/juanvaldivia/moneytrak/expenses/Expense.java
- [ ] T028 [US1] Implement ExpenseMapper.toEntity(ExpenseCreationDto) in src/main/java/dev/juanvaldivia/moneytrak/expenses/mapper/ExpenseMapper.java
- [ ] T029 [US1] Implement ExpenseMapper.toDto(Expense) in src/main/java/dev/juanvaldivia/moneytrak/expenses/mapper/ExpenseMapper.java
- [ ] T030 [US1] Implement CurrencyValidator.isValid() logic in src/main/java/dev/juanvaldivia/moneytrak/expenses/validation/CurrencyValidator.java
- [ ] T031 [US1] Implement LocalExpenseService.createExpense() method in src/main/java/dev/juanvaldivia/moneytrak/expenses/LocalExpenseService.java
- [ ] T032 [US1] Create ExpenseController with POST /v1/expenses endpoint in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseController.java
- [ ] T033 [US1] Add @Valid annotation and validation error handling to POST endpoint
- [ ] T034 [US1] Add Location header generation for POST /v1/expenses response
- [ ] T035 [US1] Run all US1 tests - verify GREEN (all passing)

**Checkpoint**: User Story 1 complete - Can record expense, returns 201 with ID, persists to database

---

## Phase 4: User Story 2 - Retrieve Recorded Expenses (Priority: P2)

**Goal**: Allow users to retrieve expense list and single expense by ID

**Independent Test**: Record expense, then GET /v1/expenses and GET /v1/expenses/{id} both return expense data

### Tests for User Story 2 (TDD: Write FIRST, ensure FAIL)

- [ ] T036 [P] [US2] Add GET /v1/expenses test to ExpenseControllerTest (MockMvc) in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T037 [P] [US2] Add GET /v1/expenses/{id} test to ExpenseControllerTest (MockMvc) in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T038 [P] [US2] Add GET /v1/expenses empty list test to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T039 [P] [US2] Add GET /v1/expenses/{id} not found test (404) to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T040 [P] [US2] Add ExpenseServiceTest.testListExpenses() in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseServiceTest.java
- [ ] T041 [P] [US2] Add ExpenseServiceTest.testGetExpenseById() in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseServiceTest.java
- [ ] T042 [US2] Add integration tests for GET endpoints to ExpenseControllerIntegrationTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerIntegrationTest.java

**TDD Checkpoint**: All US2 tests written and FAILING

### Implementation for User Story 2

- [ ] T043 [US2] Implement ExpenseRepository.findAllOrderByDateDesc() query method per data-model.md in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseRepository.java
- [ ] T044 [P] [US2] Implement LocalExpenseService.listExpenses() method in src/main/java/dev/juanvaldivia/moneytrak/expenses/LocalExpenseService.java
- [ ] T045 [P] [US2] Implement LocalExpenseService.getExpenseById() method in src/main/java/dev/juanvaldivia/moneytrak/expenses/LocalExpenseService.java
- [ ] T046 [US2] Add GET /v1/expenses endpoint to ExpenseController in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseController.java
- [ ] T047 [US2] Add GET /v1/expenses/{id} endpoint to ExpenseController in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseController.java
- [ ] T048 [US2] Add @ExceptionHandler for NotFoundException in GlobalExceptionHandler returning 404 in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/GlobalExceptionHandler.java
- [ ] T049 [US2] Run all US2 tests - verify GREEN (all passing)

**Checkpoint**: User Stories 1 AND 2 complete - Can record AND retrieve expenses independently

---

## Phase 5: User Story 3 - Update Expense Details (Priority: P3)

**Goal**: Allow users to update existing expense fields with optimistic locking

**Independent Test**: Record expense, update description with correct version, verify version increments and updatedAt changes

### Tests for User Story 3 (TDD: Write FIRST, ensure FAIL)

- [ ] T050 [P] [US3] Add PUT /v1/expenses/{id} success test to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T051 [P] [US3] Add PUT /v1/expenses/{id} version mismatch test (409 Conflict) to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T052 [P] [US3] Add PUT /v1/expenses/{id} not found test (404) to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T053 [P] [US3] Add PUT /v1/expenses/{id} validation error test (400) to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T054 [P] [US3] Add ExpenseServiceTest.testUpdateExpense() in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseServiceTest.java
- [ ] T055 [P] [US3] Add ExpenseMapperTest.testToEntity_update() for partial update mapping in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseMapperTest.java
- [ ] T056 [US3] Add integration test for concurrent update scenario (2 users, version conflict) to ExpenseControllerIntegrationTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerIntegrationTest.java

**TDD Checkpoint**: All US3 tests written and FAILING

### Implementation for User Story 3

- [ ] T057 [US3] Implement Expense.update() factory method per data-model.md in src/main/java/dev/juanvaldivia/moneytrak/expenses/Expense.java
- [ ] T058 [US3] Implement ExpenseMapper.toEntity(Expense, ExpenseUpdateDto) for partial updates in src/main/java/dev/juanvaldivia/moneytrak/expenses/mapper/ExpenseMapper.java
- [ ] T059 [US3] Implement LocalExpenseService.updateExpense() with version check in src/main/java/dev/juanvaldivia/moneytrak/expenses/LocalExpenseService.java
- [ ] T060 [US3] Add PUT /v1/expenses/{id} endpoint to ExpenseController in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseController.java
- [ ] T061 [US3] Add @ExceptionHandler for OptimisticLockException in GlobalExceptionHandler returning 409 in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/GlobalExceptionHandler.java
- [ ] T062 [US3] Run all US3 tests - verify GREEN (all passing)

**Checkpoint**: User Stories 1, 2, AND 3 complete - Full CRUD with optimistic locking working

---

## Phase 6: User Story 4 - Delete Expense Records (Priority: P3)

**Goal**: Allow users to remove expense records by ID

**Independent Test**: Record expense, delete via DELETE /v1/expenses/{id}, verify 204 and GET returns 404

### Tests for User Story 4 (TDD: Write FIRST, ensure FAIL)

- [ ] T063 [P] [US4] Add DELETE /v1/expenses/{id} success test (204) to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T064 [P] [US4] Add DELETE /v1/expenses/{id} not found test (404) to ExpenseControllerTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerTest.java
- [ ] T065 [P] [US4] Add ExpenseServiceTest.testDeleteExpense() in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseServiceTest.java
- [ ] T066 [US4] Add integration test for delete + verify not found to ExpenseControllerIntegrationTest in src/test/java/dev/juanvaldivia/moneytrak/expenses/ExpenseControllerIntegrationTest.java

**TDD Checkpoint**: All US4 tests written and FAILING

### Implementation for User Story 4

- [ ] T067 [US4] Implement LocalExpenseService.deleteExpense() method in src/main/java/dev/juanvaldivia/moneytrak/expenses/LocalExpenseService.java
- [ ] T068 [US4] Add DELETE /v1/expenses/{id} endpoint to ExpenseController in src/main/java/dev/juanvaldivia/moneytrak/expenses/ExpenseController.java
- [ ] T069 [US4] Run all US4 tests - verify GREEN (all passing)

**Checkpoint**: ALL USER STORIES COMPLETE - Full CRUD API with validation and concurrency control

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements affecting multiple stories, final validation

- [ ] T070 [P] Add JavaDoc comments to all public classes and methods in dev.juanvaldivia.moneytrak.expenses package
- [ ] T071 [P] Add @ExceptionHandler for generic Exception in GlobalExceptionHandler returning 500 in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/GlobalExceptionHandler.java
- [ ] T072 [P] Add @ExceptionHandler for MethodArgumentNotValidException in GlobalExceptionHandler for field-level validation errors in src/main/java/dev/juanvaldivia/moneytrak/expenses/exception/GlobalExceptionHandler.java
- [ ] T073 Run full test suite (./mvnw test) - verify 100% pass
- [ ] T074 Build project (./mvnw clean package) - verify success with no warnings
- [ ] T075 Start application (./mvnw spring-boot:run) - verify starts without errors
- [ ] T076 Execute manual testing from quickstart.md (all curl examples)
- [ ] T077 Verify H2 console access at http://localhost:8080/h2-console
- [ ] T078 [P] Update CLAUDE.md if any deviations from plan occurred
- [ ] T079 Run comprehensive requirements validation against spec.md (all FR-001 through FR-031)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup (Phase 1) - BLOCKS all user stories
- **User Stories (Phases 3-6)**: All depend on Foundational (Phase 2) completion
  - User stories can proceed in parallel if team capacity allows
  - Or sequentially in priority order: US1 → US2 → US3 → US4
- **Polish (Phase 7)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Depends on Foundational only - No dependencies on other stories
- **User Story 2 (P2)**: Depends on Foundational only - Independent of US1 (can record test data)
- **User Story 3 (P3)**: Depends on Foundational only - Independent (can record test data to update)
- **User Story 4 (P3)**: Depends on Foundational only - Independent (can record test data to delete)

**Key Insight**: All 4 user stories are independently testable once Foundational phase completes

### Within Each User Story (TDD Workflow)

1. **Tests FIRST**: Write all tests, ensure they FAIL (Red)
2. **Models**: Implement entity methods and DTOs
3. **Services**: Implement business logic
4. **Controllers**: Implement REST endpoints
5. **Error Handling**: Add exception handlers
6. **Verify GREEN**: Run tests, all must pass
7. **Story Complete**: Independent test criteria met

### Parallel Opportunities

**Within Phase 1 (Setup)**:
- T003, T004, T005 can all run in parallel (different files)

**Within Phase 2 (Foundational)**:
- DTOs (T008-T012) can run in parallel
- Validation (T014-T015) can run in parallel
- Exceptions (T016-T017) can run in parallel

**Across User Stories** (after Foundational completes):
- US1, US2, US3, US4 can all be developed in parallel by different developers
- Each story has independent test data setup

**Within Each User Story**:
- All test creation tasks marked [P] can run in parallel
- All independent implementation tasks marked [P] can run in parallel

---

## Parallel Example: User Story 1

### Parallel Test Creation (T021-T026)
```bash
# Launch all US1 tests together (different test files):
Task T021: "ExpenseRepositoryTest (@DataJpaTest)"
Task T022: "ExpenseMapperTest (unit)"
Task T023: "CurrencyValidatorTest (unit)"
Task T024: "ExpenseServiceTest (mocked)"
Task T025: "ExpenseControllerTest (@WebMvcTest)"
Task T026: "ExpenseControllerIntegrationTest (@SpringBootTest)"
```

### Sequential Implementation (T027-T035)
```bash
# Execute in order (some dependencies):
T027 → T028, T029, T030 (can be parallel)
→ T031 (depends on mapper)
→ T032 (depends on service)
→ T033, T034 (enhancements to controller)
→ T035 (verification)
```

---

## Parallel Example: All User Stories After Foundational

With 4 developers available:

```bash
# After Phase 2 completes, launch in parallel:
Developer A: Phase 3 (User Story 1) - T021 through T035
Developer B: Phase 4 (User Story 2) - T036 through T049
Developer C: Phase 5 (User Story 3) - T050 through T062
Developer D: Phase 6 (User Story 4) - T063 through T069

# Each story is independently testable
# Stories integrate seamlessly (same Expense entity, service interface)
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. **Complete Phase 1: Setup** (T001-T005)
   - Add dependencies, configure H2 database
   - Estimated: 30 minutes

2. **Complete Phase 2: Foundational** (T006-T020)
   - Build all shared infrastructure (entity, DTOs, mappers, exceptions)
   - CRITICAL: Must finish before ANY user story work
   - Estimated: 4-6 hours

3. **Complete Phase 3: User Story 1** (T021-T035)
   - TDD: Write tests first, ensure they fail
   - Implement POST /v1/expenses endpoint
   - Verify: Can record expense, returns 201 with ID
   - Estimated: 4-6 hours

4. **STOP and VALIDATE**
   - Run: `./mvnw test` - All US1 tests pass
   - Manual test: `curl -X POST ... ` from quickstart.md
   - Access H2 console, verify expense persisted
   - **YOU NOW HAVE AN MVP!**

5. **Optional**: Deploy MVP to staging/production

### Incremental Delivery (Build on MVP)

1. **Foundation + US1** → Test → Deploy (MVP! ✅)
2. **+ US2** (T036-T049) → Test independently → Deploy
   - Now users can record AND retrieve expenses
3. **+ US3** (T050-T062) → Test independently → Deploy
   - Now users can update expenses with conflict detection
4. **+ US4** (T063-T069) → Test independently → Deploy
   - Now full CRUD operations available
5. **+ Polish** (T070-T079) → Final validation → Production release

**Each story adds value without breaking previous functionality**

### Parallel Team Strategy (4 Developers)

**Week 1**:
- All developers: Phase 1 + Phase 2 together (pair/mob programming)
- Result: Solid foundation, everyone understands architecture

**Week 2**:
- Developer A: User Story 1 (MVP)
- Developer B: User Story 2
- Developer C: User Story 3
- Developer D: User Story 4
- Result: All 4 stories complete in parallel

**Week 3**:
- All developers: Phase 7 (Polish), integration testing, deployment prep
- Result: Production-ready release

---

## Test Coverage Targets (Constitution Requirements)

Per Constitution testing strategy:

- **Unit Tests (70%)**: T022-T024, T040-T041, T054-T055, T065
  - Service logic with mocked repositories
  - Mappers (DTO ↔ Domain)
  - Custom validators

- **Integration Tests (20%)**: T021, T026, T042, T056, T066
  - Controller + service with real database
  - JPA entity mappings (@DataJpaTest)
  - Full request/response cycles (@SpringBootTest)

- **Contract Tests (10%)**: T025, T036-T039, T050-T053, T063-T064
  - HTTP contract validation (@WebMvcTest)
  - Status codes, headers, JSON structure
  - Validation error formats

**Coverage Requirements**:
- Minimum 80% line coverage for service layer (LocalExpenseService)
- 100% coverage for validation rules (all FR requirements)
- All error handling paths tested

---

## Notes

- **[P] tasks**: Different files, can run in parallel
- **[Story] label**: Maps task to user story (US1-US4) for traceability
- **TDD Non-Negotiable**: All tests written FIRST, must FAIL before implementation
- **Independent Stories**: Each user story can be completed and deployed independently
- **Checkpoint Validation**: Stop after each story to verify independent functionality
- **File Paths**: All paths are exact, ready for execution
- **Commit Strategy**: Commit after each task or logical group (e.g., all tests for one story)
- **Constitution Compliance**: Feature-based architecture (expenses package), DTO pattern, type safety, TDD, versioning all enforced

**Total Tasks**: 79
- Setup: 5 tasks
- Foundational: 15 tasks
- User Story 1: 15 tasks (6 tests + 9 implementation)
- User Story 2: 14 tasks (7 tests + 7 implementation)
- User Story 3: 13 tasks (7 tests + 6 implementation)
- User Story 4: 7 tasks (4 tests + 3 implementation)
- Polish: 10 tasks

**Suggested MVP**: Phase 1 + Phase 2 + Phase 3 (35 tasks) = Fully functional expense recording API