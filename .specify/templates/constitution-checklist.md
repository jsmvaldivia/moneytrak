# Constitution Compliance Checklist: [FEATURE NAME]

**Feature**: [Link to spec.md]  
**Date**: [DATE]

## Core Principles (Must Have)

### Architecture
- [ ] Feature in package: `dev.juanvaldivia.moneytrak.<feature>/`
- [ ] DTOs in `dto/` subdirectory, Mappers in `mapper/` subdirectory
- [ ] No cross-feature dependencies except through service interfaces

### API Contracts
- [ ] Controller uses DTOs (not entities directly)
- [ ] `<Feature>CreationDto`, `<Feature>UpdateDto`, `<Feature>Dto` exist
- [ ] DTOs are records; Entities are classes with @Entity
- [ ] Mapper handles DTO ↔ Entity conversion

### Testing (TDD)
- [ ] Tests written before implementation
- [ ] Integration tests for all endpoints (@SpringBootTest)
- [ ] Validation rules have test cases

### API Design
- [ ] All endpoints use `/v1/` prefix
- [ ] POST returns 201 with Location header
- [ ] Changes documented in CLAUDE.md

### Type Safety
- [ ] BigDecimal for money (never double/float)
- [ ] ZonedDateTime for dates
- [ ] UUID for entity IDs
- [ ] @Version field for optimistic locking
- [ ] Enums use @Enumerated(STRING)
- [ ] Validation annotations on DTOs with @Valid

### Security (if applicable)
- [ ] Role-based access (APP/BACKOFFICE/ADMIN) in SecurityFilterChain
- [ ] 401/403 errors use ErrorResponseDto format
- [ ] Security tests for all roles

### Database (if applicable)
- [ ] Flyway migration: `V[n]__[description].sql`
- [ ] Migration tested (no data loss)
- [ ] Foreign key indexes added

## Build & Quality

- [ ] `./mvnw clean package` succeeds
- [ ] `./mvnw test` passes (100%)
- [ ] No compiler warnings
- [ ] JavaDoc on public APIs

## Deviations

| Item | Reason | Approver |
|------|--------|----------|
| | | |

---

**Compliant**: [ ] Yes [ ] No  
**Reviewer**: [NAME]
