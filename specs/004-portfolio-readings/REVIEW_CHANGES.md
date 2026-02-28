# Specification Review Changes Summary

**Date**: 2026-02-28
**Reviewer**: Deep Review (16 issues across 4 sections)
**Status**: All changes applied to spec.md

## Changes Applied

### Section 1: Requirements & Scope (4 issues)

#### **Issue #1: Missing GET /v1/accounts endpoint** ✅
- **Decision**: Add endpoint to list all accounts
- **Changes**:
  - Added FR-009A: "System MUST provide endpoint to list all accounts ordered by name, then by ID"
  - Added US1 Scenario 2: Test listing 3 accounts with alphabetical ordering
  - Added to API design

#### **Issue #2: Six unanswered edge cases** ✅
- **Decision**: Resolve all 6 edge cases now
- **Changes**:
  - Replaced edge case questions with resolved decisions
  - EC1: Allow duplicate readings (no uniqueness constraint)
  - EC2: Support 8 decimal places for crypto
  - EC3: Hard limit 1000 accounts (no pagination)
  - EC4: AccountId immutable after creation
  - EC5: Soft-deleted = read-only tombstone
  - EC6: Allow negative amounts (for debts/margins)
  - Updated assumptions section to reflect these decisions

#### **Issue #3: Missing GET /v1/readings/{id} endpoint** ✅
- **Decision**: Add endpoint to retrieve single reading
- **Changes**:
  - Added FR-012B: "System MUST provide endpoint to retrieve a single reading by ID"
  - Added US2 Scenario 2: Test retrieving reading with account details
  - Added to API design

#### **Issue #4: Contradictory amount validation** ✅
- **Decision**: Allow negative amounts for debts/margins
- **Changes**:
  - Added FR-012A: "System MUST allow zero and negative amounts"
  - Updated Reading entity validation: "amount can be positive, zero, or negative"
  - Removed contradiction between line 164 and 223
  - Added US2 Scenario 7: Test negative amount for margin account

---

### Section 2: Design Clarity (4 issues)

#### **Issue #5: Implementation details leak into spec** ✅
- **Decision**: Make spec technology-agnostic
- **Changes**:
  - Changed "deleted (boolean flag for soft delete)" → "soft deletion marker"
  - Changed "set deleted=true" → "readings are marked as deleted"
  - Removed database column implementation details
  - Kept behavioral description only

#### **Issue #6: Non-deterministic ordering** ✅
- **Decision**: Add secondary sort by account ID
- **Changes**:
  - Updated FR-025: "ordered by account name, then by account ID as tiebreaker"
  - Updated FR-009A: Same ordering for account list
  - Ensures deterministic results for testing
  - Added to US1 Scenario 2

#### **Issue #7: No individual account reading history** ✅
- **Decision**: Add GET /v1/accounts/{id}/readings endpoint
- **Changes**:
  - Added new US6 - Account Reading History (Priority P3)
  - Added FR-026A: Endpoint to retrieve all readings for specific account
  - Added FR-026B: Exclude soft-deleted readings from history
  - Added 5 acceptance scenarios for US6
  - Removed from Out of Scope section

#### **Issue #8: Account property updates unclear** ✅
- **Decision**: Document retroactive updates as intended behavior
- **Changes**:
  - Updated assumption: "Account updates apply to all past readings retroactively (readings reflect current account properties, not historical snapshots)"
  - Clarified this is intentional design for v1 simplicity
  - No versioning of account properties in v1

---

### Section 3: Testing Strategy (4 issues)

#### **Issue #9: No optimistic locking tests** ✅
- **Decision**: Add scenarios to US1 and US2
- **Changes**:
  - Added US1 Scenario 5: Concurrent account update → 409 Conflict
  - Added US2 Scenario 6: Concurrent reading update → 409 Conflict
  - Tests version field behavior and conflict detection

#### **Issue #10: New endpoints not tested** ✅
- **Decision**: Add acceptance scenarios to existing user stories
- **Changes**:
  - US1 Scenario 2: Test GET /v1/accounts (list)
  - US2 Scenario 2: Test GET /v1/readings/{id} (single reading)
  - US1 Scenario 8: Test 1000 account limit enforcement

#### **Issue #11: Soft-deleted readings block account deletion** ✅
- **Decision**: Allow deletion if only soft-deleted readings
- **Changes**:
  - Updated FR-005: "prevent deletion of accounts that have active readings (soft-deleted readings do not prevent deletion)"
  - Updated FR-006: Error message shows "active reading count"
  - Updated US5 description and scenarios
  - Added US5 Scenario 2: Soft-deleted readings don't prevent deletion
  - Added US5 Scenario 3: Mixed active+soft-deleted shows only active count

#### **Issue #12: Account reading history not tested** ✅
- **Decision**: Add new US6 with dedicated test scenarios
- **Changes**:
  - Created US6 - Account Reading History (5 acceptance scenarios)
  - Tests chronological ordering, soft-delete exclusion, empty lists, 404 handling
  - Independent user story focused on historical trend analysis

---

### Section 4: Performance & Scalability (4 issues)

#### **Issue #13: No index strategy** ✅
- **Decision**: Document index requirements in Dependencies
- **Changes**:
  - Added new "Performance Dependencies" section
  - Specified indexes on Reading(accountId, readingDate) and Account(name)
  - Required for 10,000+ readings performance (SC-004)

#### **Issue #14: N+1 query risk** ✅
- **Decision**: Add performance note to SC-003 (not FR-026)
- **Changes**:
  - Updated SC-003: "must avoid N+1 queries through JOINs or batch fetching"
  - Added to Performance Dependencies: "Query optimization required"
  - Balances clarity with implementation flexibility

#### **Issue #15: Soft-delete bloat** ✅
- **Decision**: Document as Out of Scope for v1
- **Changes**:
  - Added to Out of Scope: "Soft-delete cleanup/archival (requires backup/restore strategy in future version)"
  - Updated assumption: "Soft-deleted readings become read-only tombstones"
  - Acknowledged as known limitation for v1

#### **Issue #16: No circuit breaker for 1000 account limit** ✅
- **Decision**: Add explicit FR for limit enforcement
- **Changes**:
  - Added FR-009B: "System MUST enforce hard limit of 1000 accounts, returning 409 Conflict"
  - Added US1 Scenario 8: Test limit enforcement with clear error message
  - Added Account entity constraint: "Maximum 1000 accounts per user"
  - Updated assumption: "System enforces hard limit of 1000 accounts"

---

## API Changes Summary

### **New Endpoints Added**:
1. `GET /v1/accounts` - List all accounts (alphabetically ordered)
2. `GET /v1/readings/{id}` - Retrieve single reading with account details
3. `GET /v1/accounts/{id}/readings` - Retrieve reading history for one account

### **Validation Changes**:
- Amounts: Support 8 decimal places (was 2)
- Amounts: Allow negative values (for debts/margins)
- Account limit: Hard limit of 1000 accounts enforced

### **Behavior Changes**:
- Account deletion: Allow if only soft-deleted readings remain (was: block all deletions with any readings)
- Ordering: All lists use deterministic secondary sort by ID (for duplicate names/dates)

---

## Functional Requirements Added

- **FR-009A**: List all accounts endpoint
- **FR-009B**: 1000 account limit enforcement
- **FR-012A**: Allow zero/negative amounts
- **FR-012B**: Get single reading by ID endpoint
- **FR-026A**: Get account reading history endpoint
- **FR-026B**: Exclude soft-deleted from history

---

## User Story Changes

### **US1 - Account Management** (3 new scenarios):
- Scenario 2: List accounts
- Scenario 5: Optimistic locking test
- Scenario 8: Account limit enforcement

### **US2 - Portfolio Snapshot Recording** (3 new scenarios):
- Scenario 2: Retrieve single reading
- Scenario 6: Optimistic locking test
- Scenarios 7-8: Negative amounts & crypto precision

### **US5 - Account Deletion Constraints** (Updated):
- Changed logic: Soft-deleted readings don't prevent deletion
- Added scenario for mixed active/soft-deleted

### **US6 - Account Reading History** (NEW):
- 5 acceptance scenarios for reading history endpoint

---

## Quality Improvements

✅ **DRY**: No violations introduced
✅ **Testing**: Added 11 new acceptance scenarios (optimistic locking, new endpoints, edge cases)
✅ **Engineering**: Balanced - not over-engineered (deferred cleanup to v2) or under-engineered (added reading history)
✅ **Edge cases**: All 6 edge cases resolved and documented
✅ **Explicit**: Removed implementation details, documented behavior clearly

---

## Risk Acknowledgments

⚠️ **Accepted Risk (Issue #14)**: N+1 query optimization left to implementation team
- **Mitigation**: Added performance note to SC-003 and Performance Dependencies section
- **Rationale**: Trust ORM (Hibernate) with proper fetch strategies

⚠️ **Known Limitation (Issue #15)**: Soft-delete bloat over time
- **Mitigation**: Documented in Out of Scope, planned for v2 cleanup capability
- **Rationale**: Acceptable for v1, avoid over-engineering archival strategy

---

## Next Steps

1. ✅ Spec updated and validated
2. 📋 Ready for `/speckit.plan` - Create implementation plan
3. ✅ All edge cases resolved
4. ✅ All endpoints defined with acceptance criteria
5. ✅ Performance considerations documented

---

**Spec Status**: ✅ **READY FOR IMPLEMENTATION PLANNING**
