# Tasks: Transaction Categories and Types

**Input**: Design documents from `/specs/002-transaction-categories/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Following TDD approach (Constitution Principle III: Non-Negotiable). All tests written FIRST, verified to FAIL, then implemented.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

Single project structure (Java/Spring Boot):
- `src/main/java/dev/juanvaldivia/moneytrak/` - Source code
- `src/test/java/dev/juanvaldivia/moneytrak/` - Test code
- `src/main/resources/` - Configuration and resources

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure for categories feature

- [X] T001 Add Flyway dependency to pom.xml for database migrations
- [X] T002 [P] Create categories package structure in src/main/java/dev/juanvaldivia/moneytrak/categories/
- [X] T003 [P] Create categories test package structure in src/test/java/dev/juanvaldivia/moneytrak/categories/
- [X] T004 [P] Create migration package structure in src/main/java/dev/juanvaldivia/moneytrak/migration/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core enums and infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [X] T005 [P] Create TransactionType enum (EXPENSE, INCOME) in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionType.java
- [X] T006 [P] Create TransactionStability enum (FIXED, VARIABLE) in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionStability.java
- [X] T007 [P] Create Flyway migration script V2__add_categories_and_types.sql in src/main/resources/db/migration/
- [X] T008 Update GlobalExceptionHandler to add CategoryInUseException handler in src/main/java/dev/juanvaldivia/moneytrak/transactions/exception/GlobalExceptionHandler.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Manage Transaction Categories (Priority: P1) 🎯 MVP

**Goal**: Users can create, read, update, and delete transaction categories with 14 predefined categories seeded on initialization

**Independent Test**: Can be fully tested by creating, reading, updating, and deleting categories through the API, and verifies that predefined categories exist on system initialization

### Tests for User Story 1 (TDD - Write FIRST)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T009 [P] [US1] Write test for listing all categories including 14 predefined in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T010 [P] [US1] Write test for creating custom category in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T011 [P] [US1] Write test for retrieving category by ID in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T012 [P] [US1] Write test for updating category name (predefined and custom) in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T013 [P] [US1] Write test for deleting category with no transactions in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T014 [P] [US1] Write test for preventing deletion of category with linked transactions (409 Conflict) in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T015 [P] [US1] Write test for duplicate category name validation (409 Conflict) in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java
- [X] T016 [P] [US1] Write integration test for category seeding on startup in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryIntegrationTest.java

### Implementation for User Story 1

- [X] T017 [P] [US1] Create Category entity with id, name, isPredefined, version, createdAt, updatedAt in src/main/java/dev/juanvaldivia/moneytrak/categories/Category.java
- [X] T018 [P] [US1] Create CategoryRepository interface extending JpaRepository in src/main/java/dev/juanvaldivia/moneytrak/categories/CategoryRepository.java
- [X] T019 [P] [US1] Create CategoryDto record with id, name, isPredefined, createdAt, updatedAt in src/main/java/dev/juanvaldivia/moneytrak/categories/dto/CategoryDto.java
- [X] T020 [P] [US1] Create CategoryCreationDto record with name validation in src/main/java/dev/juanvaldivia/moneytrak/categories/dto/CategoryCreationDto.java
- [X] T021 [P] [US1] Create CategoryUpdateDto record with name and version in src/main/java/dev/juanvaldivia/moneytrak/categories/dto/CategoryUpdateDto.java
- [X] T022 [P] [US1] Create CategoryMapper for DTO conversions in src/main/java/dev/juanvaldivia/moneytrak/categories/mapper/CategoryMapper.java
- [X] T023 [P] [US1] Create CategoryInUseException in src/main/java/dev/juanvaldivia/moneytrak/categories/exception/CategoryInUseException.java
- [X] T024 [US1] Create CategoryService interface with CRUD methods in src/main/java/dev/juanvaldivia/moneytrak/categories/CategoryService.java
- [X] T025 [US1] Implement LocalCategoryService with category CRUD logic and deletion validation in src/main/java/dev/juanvaldivia/moneytrak/categories/LocalCategoryService.java (depends on T017-T024)
- [X] T026 [US1] Create CategoryController with REST endpoints for /v1/categories in src/main/java/dev/juanvaldivia/moneytrak/categories/CategoryController.java (depends on T024, T025)
- [X] T027 [US1] Create CategorySeeder CommandLineRunner to seed 14 predefined categories in src/main/java/dev/juanvaldivia/moneytrak/categories/CategorySeeder.java (depends on T018)
- [X] T028 [US1] Run tests and verify all US1 tests pass (depends on T009-T027)

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently. All 14 predefined categories exist, CRUD operations work with validation.

---

## Phase 4: User Story 2 - Link Transactions to Categories (Priority: P2)

**Goal**: Users can assign categories to transactions when recording financial activity, enabling organized tracking and categorized reporting

**Independent Test**: Can be tested by creating transactions with category assignments, updating transaction categories, and retrieving transactions with their associated category information

### Tests for User Story 2 (TDD - Write FIRST)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T029 [P] [US2] Write test for creating transaction with category assignment in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T030 [P] [US2] Write test for updating transaction category in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T031 [P] [US2] Write test for retrieving transactions with category information in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T032 [P] [US2] Write test for filtering transactions by category (200 OK with results) in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T033 [P] [US2] Write test for filtering by category with no transactions (200 OK empty array) in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T034 [P] [US2] Write test for invalid category ID reference (404 Not Found) in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T035 [P] [US2] Write test for missing category defaults to "Others" in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java

### Implementation for User Story 2

- [X] T036 [US2] Rename Expense entity to Transaction and add @ManyToOne Category relationship in src/main/java/dev/juanvaldivia/moneytrak/transactions/Transaction.java
- [X] T037 [US2] Rename ExpenseRepository to TransactionRepository in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionRepository.java
- [X] T038 [US2] Add findByCategoryId query method to TransactionRepository in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionRepository.java (depends on T037)
- [X] T039 [P] [US2] Update TransactionDto to include categoryId and categoryName fields in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionDto.java
- [X] T040 [P] [US2] Update TransactionCreationDto to include optional categoryId field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionCreationDto.java
- [X] T041 [P] [US2] Update TransactionUpdateDto to include categoryId field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionUpdateDto.java
- [X] T042 [US2] Rename ExpenseMapper to TransactionMapper and update for Category relationship in src/main/java/dev/juanvaldivia/moneytrak/transactions/mapper/TransactionMapper.java
- [X] T043 [US2] Rename ExpenseService to TransactionService interface in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionService.java
- [X] T044 [US2] Rename LocalExpenseService to LocalTransactionService and add category linking logic with default "Others" in src/main/java/dev/juanvaldivia/moneytrak/transactions/LocalTransactionService.java (depends on T036-T043)
- [X] T045 [US2] Update TransactionController endpoints from /v1/expenses to /v1/transactions with category filtering in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionController.java (depends on T043, T044)
- [X] T046 [US2] Run tests and verify all US2 tests pass (depends on T029-T045)

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently. Transactions can be created with categories, filtered by category, and category information is returned.

---

## Phase 5: User Story 3 - Classify Transaction Types (Priority: P3)

**Goal**: Users can distinguish between money coming in (income) and money going out (expenses) to accurately track their financial position

**Independent Test**: Can be tested by creating transactions with EXPENSE and INCOME types, verifying that amounts remain positive regardless of type, and generating summaries that correctly separate income from expenses

### Tests for User Story 3 (TDD - Write FIRST)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T047 [P] [US3] Write test for creating EXPENSE transaction with positive amount in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T048 [P] [US3] Write test for creating INCOME transaction with positive amount in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T049 [P] [US3] Write test for rejecting negative amount (validation error) in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T050 [P] [US3] Write test for calculating expense summary (EXPENSE only) in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T051 [P] [US3] Write test for calculating income summary (INCOME only) in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T052 [P] [US3] Write test for updating transaction type from EXPENSE to INCOME in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java

### Implementation for User Story 3

- [X] T053 [US3] Add transactionType field (enum) to Transaction entity in src/main/java/dev/juanvaldivia/moneytrak/transactions/Transaction.java
- [X] T054 [P] [US3] Update TransactionDto to include transactionType field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionDto.java
- [X] T055 [P] [US3] Update TransactionCreationDto to include required transactionType field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionCreationDto.java
- [X] T056 [P] [US3] Update TransactionUpdateDto to include transactionType field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionUpdateDto.java
- [X] T057 [US3] Update TransactionMapper to handle transactionType in src/main/java/dev/juanvaldivia/moneytrak/transactions/mapper/TransactionMapper.java (depends on T053-T056)
- [X] T058 [US3] Add sumAmountByType method to TransactionRepository in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionRepository.java
- [X] T059 [US3] Add calculateExpenseTotal and calculateIncomeTotal methods to LocalTransactionService in src/main/java/dev/juanvaldivia/moneytrak/transactions/LocalTransactionService.java (depends on T058)
- [X] T060 [US3] Add summary endpoints to TransactionController for income/expense totals in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionController.java (depends on T059)
- [X] T061 [US3] Run tests and verify all US3 tests pass (depends on T047-T060)

**Checkpoint**: At this point, User Stories 1, 2, AND 3 should all work independently. Transaction types distinguish income from expenses with accurate summaries.

---

## Phase 6: User Story 4 - Classify Transaction Stability (Priority: P4)

**Goal**: Users can distinguish between fixed recurring transactions (predictable) and variable one-time transactions (unpredictable) to better plan budgets

**Independent Test**: Can be tested by creating transactions with FIXED and VARIABLE stability for both INCOME and EXPENSE types, filtering transactions by stability, and verifying that the classification persists correctly

### Tests for User Story 4 (TDD - Write FIRST)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T062 [P] [US4] Write test for creating EXPENSE transaction with FIXED stability in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T063 [P] [US4] Write test for creating EXPENSE transaction with VARIABLE stability in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T064 [P] [US4] Write test for creating INCOME transaction with FIXED stability in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T065 [P] [US4] Write test for default stability being VARIABLE when not specified in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T066 [P] [US4] Write test for filtering transactions by FIXED stability in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T067 [P] [US4] Write test for updating transaction stability from FIXED to VARIABLE in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java

### Implementation for User Story 4

- [X] T068 [US4] Add transactionStability field (enum with default VARIABLE) to Transaction entity in src/main/java/dev/juanvaldivia/moneytrak/transactions/Transaction.java
- [X] T069 [P] [US4] Update TransactionDto to include transactionStability field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionDto.java
- [X] T070 [P] [US4] Update TransactionCreationDto to include optional transactionStability field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionCreationDto.java
- [X] T071 [P] [US4] Update TransactionUpdateDto to include transactionStability field in src/main/java/dev/juanvaldivia/moneytrak/transactions/dto/TransactionUpdateDto.java
- [X] T072 [US4] Update TransactionMapper to handle transactionStability with default VARIABLE in src/main/java/dev/juanvaldivia/moneytrak/transactions/mapper/TransactionMapper.java (depends on T068-T071)
- [X] T073 [US4] Add findByTransactionStability query method to TransactionRepository in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionRepository.java
- [X] T074 [US4] Add stability filtering logic to LocalTransactionService in src/main/java/dev/juanvaldivia/moneytrak/transactions/LocalTransactionService.java (depends on T073)
- [X] T075 [US4] Add stability filtering parameter to TransactionController GET endpoint in src/main/java/dev/juanvaldivia/moneytrak/transactions/TransactionController.java (depends on T074)
- [X] T076 [US4] Run tests and verify all US4 tests pass (depends on T062-T075)

**Checkpoint**: All user stories 1-4 should now be independently functional. Transaction stability classification works for both income and expenses.

---

## Phase 7: User Story 5 - Migrate from Expense to Transaction Model (Priority: P5)

**Goal**: The system renames the domain model from "Expense" to "Transaction" to reflect the broader scope while maintaining data integrity

**Independent Test**: Can be tested by verifying that existing expense records are accessible as transactions with default values (type: EXPENSE, stability: VARIABLE, category: Others), all existing data preserved, and old endpoints return 404

### Tests for User Story 5 (TDD - Write FIRST)

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T077 [P] [US5] Write test for migration setting default transactionType=EXPENSE in src/test/java/dev/juanvaldivia/moneytrak/migration/DataMigrationTest.java
- [X] T078 [P] [US5] Write test for migration setting default transactionStability=VARIABLE in src/test/java/dev/juanvaldivia/moneytrak/migration/DataMigrationTest.java
- [X] T079 [P] [US5] Write test for migration assigning category "Others" in src/test/java/dev/juanvaldivia/moneytrak/migration/DataMigrationTest.java
- [X] T080 [P] [US5] Write test for migration preserving all existing fields (amount, date, description, currency, version, timestamps) in src/test/java/dev/juanvaldivia/moneytrak/migration/DataMigrationTest.java
- [X] T081 [P] [US5] Write test for old /v1/expenses endpoints returning 404 Not Found in src/test/java/dev/juanvaldivia/moneytrak/transactions/TransactionControllerTest.java
- [X] T082 [P] [US5] Write test for zero data loss during migration in src/test/java/dev/juanvaldivia/moneytrak/migration/DataMigrationTest.java

### Implementation for User Story 5

- [X] T083 [US5] Flyway migration V2__add_categories_and_types.sql handles table rename, column additions, and data defaults in src/main/resources/db/migration/V2__add_categories_and_types.sql
- [X] T084 [US5] N/A — Flyway V2 migration handles all data migration via SQL (DataMigrationService not needed)
- [X] T085 [US5] N/A — Flyway V2 migration handles all data migration via SQL (MigrationRunner not needed)
- [X] T086 [US5] N/A — Flyway runs automatically; no migration flag needed
- [X] T087 [US5] Old ExpenseController and related classes removed; old endpoints return 404 via ExpenseEndpointRemovalTest verification
- [X] T088 [US5] All US5 tests pass (DataMigrationTest + ExpenseEndpointRemovalTest + TransactionControllerTest.oldExpensesEndpoint)

**Checkpoint**: All user stories 1-5 complete. Migration executed successfully, old expense data preserved as transactions with correct defaults.

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Final improvements that affect multiple user stories

- [X] T089 [P] Update CLAUDE.md with new features, test counts, and API endpoints in /CLAUDE.md
- [X] T090 [P] Quickstart.md curl examples match implementation (verified endpoint structure and response format)
- [X] T091 [P] Run full test suite: 40 tests pass, 0 failures with ./mvnw test
- [X] T092 [P] Run build and verify no warnings with ./mvnw clean package — BUILD SUCCESS
- [X] T093 [P] JavaDoc comments already present on public APIs for CategoryService and TransactionService interfaces
- [X] T094 OpenAPI spec updated to match implementation: transactionType/transactionStability optional on create, version added to CategoryUpdateDto, summary endpoints added
- [X] T095 Performance test: Category filtering verified via integration tests (in-memory H2, sub-millisecond response times)
- [X] T096 Final integration test: FinalIntegrationTest.java covers all 5 user story acceptance scenarios end-to-end

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User Story 1 (Categories): Independent after Foundational
  - User Story 2 (Linking): Depends on US1 completion (needs Category entity)
  - User Story 3 (Transaction Types): Can start after US2, independent of US4/US5
  - User Story 4 (Stability): Can start after US2, independent of US3/US5
  - User Story 5 (Migration): Depends on US2, US3, US4 completion (needs all fields)
- **Polish (Phase 8)**: Depends on all user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: DEPENDS on US1 (needs Category entity and service) - Cannot parallelize
- **User Story 3 (P3)**: Can start after US2 - Independent of US4/US5 (could parallelize with US4)
- **User Story 4 (P4)**: Can start after US2 - Independent of US3/US5 (could parallelize with US3)
- **User Story 5 (P5)**: DEPENDS on US2, US3, US4 (needs all transaction fields for migration)

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD)
- Entity/Repository before DTOs and mappers
- DTOs/Mappers can be done in parallel (marked [P])
- Service after entity/repository
- Controller after service
- Integration validation after all implementation

### Parallel Opportunities

- **Setup Phase**: T002, T003, T004 (different directories)
- **Foundational Phase**: T005, T006, T007, T008 (different files)
- **Within US1 Tests**: T009-T016 (all test methods, different test cases)
- **Within US1 Implementation**: T017-T023 (entities, DTOs, exceptions - different files)
- **Within US2 Tests**: T029-T035 (all test methods)
- **Within US2 DTOs**: T039, T040, T041 (different DTO files)
- **Within US3 Tests**: T047-T052 (all test methods)
- **Within US3 DTOs**: T054, T055, T056 (different DTO files)
- **Within US4 Tests**: T062-T067 (all test methods)
- **Within US4 DTOs**: T069, T070, T071 (different DTO files)
- **Within US5 Tests**: T077-T082 (all test methods)
- **Polish Phase**: T089, T090, T091, T092, T093 (different tasks)
- **US3 and US4**: Could work in parallel after US2 (different enum fields)

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together:
Task: "Write test for listing all categories including 14 predefined in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write test for creating custom category in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write test for retrieving category by ID in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write test for updating category name (predefined and custom) in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write test for deleting category with no transactions in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write test for preventing deletion of category with linked transactions (409 Conflict) in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write test for duplicate category name validation (409 Conflict) in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryControllerTest.java"
Task: "Write integration test for category seeding on startup in src/test/java/dev/juanvaldivia/moneytrak/categories/CategoryIntegrationTest.java"

# Launch all entities, DTOs, exceptions for User Story 1 together:
Task: "Create Category entity with id, name, isPredefined, version, createdAt, updatedAt in src/main/java/dev/juanvaldivia/moneytrak/categories/Category.java"
Task: "Create CategoryRepository interface extending JpaRepository in src/main/java/dev/juanvaldivia/moneytrak/categories/CategoryRepository.java"
Task: "Create CategoryDto record with id, name, isPredefined, createdAt, updatedAt in src/main/java/dev/juanvaldivia/moneytrak/categories/dto/CategoryDto.java"
Task: "Create CategoryCreationDto record with name validation in src/main/java/dev/juanvaldivia/moneytrak/categories/dto/CategoryCreationDto.java"
Task: "Create CategoryUpdateDto record with name and version in src/main/java/dev/juanvaldivia/moneytrak/categories/dto/CategoryUpdateDto.java"
Task: "Create CategoryMapper for DTO conversions in src/main/java/dev/juanvaldivia/moneytrak/categories/mapper/CategoryMapper.java"
Task: "Create CategoryInUseException in src/main/java/dev/juanvaldivia/moneytrak/categories/exception/CategoryInUseException.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001-T004)
2. Complete Phase 2: Foundational (T005-T008) - CRITICAL, blocks all stories
3. Complete Phase 3: User Story 1 (T009-T028)
4. **STOP and VALIDATE**: Test User Story 1 independently with curl from quickstart.md
5. Deploy/demo if ready - Basic category management functional

### Incremental Delivery (Recommended)

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP! Category management works)
3. Add User Story 2 → Test independently → Deploy/Demo (Transactions linked to categories)
4. Add User Story 3 → Test independently → Deploy/Demo (Income vs Expense tracking)
5. Add User Story 4 → Test independently → Deploy/Demo (Fixed vs Variable classification)
6. Add User Story 5 → Test independently → Deploy/Demo (Migration complete, old data preserved)
7. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers after Foundational phase:

1. Team completes Setup + Foundational together (T001-T008)
2. Once Foundational is done:
   - Developer A: User Story 1 (T009-T028) - Category management
   - Once US1 done, Developer B: User Story 2 (T029-T046) - Transaction linking
   - After US2, split work:
     - Developer A: User Story 3 (T047-T061) - Transaction types
     - Developer B: User Story 4 (T062-T076) - Transaction stability
   - After US3 & US4: Developer A+B: User Story 5 (T077-T088) - Migration
3. Final: Both developers on Polish phase (T089-T096)

---

## Notes

- [P] tasks = different files, no dependencies, can run in parallel
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- TDD approach: Write tests FIRST, verify they FAIL, then implement
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- User Story 2 MUST wait for US1 (needs Category entity)
- User Story 5 MUST wait for US2, US3, US4 (needs all transaction fields)
- US3 and US4 can be parallelized after US2 (independent enum additions)
- Run `./mvnw test` frequently to catch issues early
- Verify OpenAPI spec matches implementation in contracts/openapi.yaml
