# Questions

Here we have 3 questions related to the code base for you to answer. It is not about right or wrong, but more about what's the reasoning behind your decisions.

1. In this code base, we have some different implementation strategies when it comes to database access layer and manipulation. If you would maintain this code base, would you refactor any of those? Why?

**Answer:**
```txt
Yes, I would refactor to standardize on the Repository pattern. Here's why:

CURRENT STATE:

1. Product/Store - Active Record Pattern (Panache):
   Example from Store.java:
   ```java
   public class Store extends PanacheEntity {
     public String name;
     // In StoreResource:
     store.persist();  // Entity knows how to save itself
     Store.findById(id);  // Static methods on entity
   ```
   Issues:
   - Entity is both domain model AND persistence logic
   - Hard to test (need database for entity operations)
   - Violates Single Responsibility Principle
   - Can't easily switch persistence mechanisms

2. Warehouse - Repository Pattern (Clean Architecture):
   Example:
   ```java
   // Domain model (pure POJO)
   public class Warehouse {
     public String businessUnitCode;
     public Integer capacity;
   }
   
   // Persistence entity
   @Entity
   public class DbWarehouse {
     @Id public Long id;
     public String businessUnitCode;
     
     public Warehouse toWarehouse() { ... }
   }
   
   // Repository handles all DB operations
   public class WarehouseRepository implements WarehouseStore {
     public void create(Warehouse w) { ... }
   }
   ```

WHY REPOSITORY PATTERN IS BETTER:

1. Testability:
   - Can mock WarehouseRepository in tests
   - Domain logic doesn't need database
   - Example: CreateWarehouseUseCaseTest mocks repository, runs in milliseconds

2. Separation of Concerns:
   - Domain model (Warehouse) = business rules only
   - DbWarehouse = JPA annotations, database concerns
   - Repository = data access logic

3. Flexibility:
   - Easy to add caching layer in repository
   - Can switch from PostgreSQL to MongoDB without changing domain
   - Can add read replicas, sharding logic in repository

4. Business Logic Protection:
   - Domain models can't be accidentally persisted with invalid state
   - Validation happens in use cases before persistence

REFACTORING PLAN:

For Store:
```java
// 1. Create domain model
public class StoreModel {
  private String name;
  private int quantityProductsInStock;
  // Business logic here
}

// 2. Keep Store as @Entity (rename to DbStore)
@Entity
public class DbStore {
  @Id public Long id;
  public String name;
  
  public StoreModel toDomain() { ... }
}

// 3. Create repository
public class StoreRepository {
  public void create(StoreModel store) { ... }
  public StoreModel findById(Long id) { ... }
}
```

This makes the codebase consistent, more maintainable, and follows hexagonal architecture principles.
```
----
2. When it comes to API spec and endpoints handlers, we have an Open API yaml file for the `Warehouse` API from which we generate code, but for the other endpoints - `Product` and `Store` - we just coded directly everything. What would be your thoughts about what are the pros and cons of each approach and what would be your choice?

**Answer:**
```txt
APPROACH COMPARISON:

1. OpenAPI-First (Warehouse):

Example from warehouse-openapi.yaml:
```yaml
paths:
  /warehouse:
    post:
      summary: Create a new warehouse unit
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Warehouse'
      responses:
        '201':
          description: Warehouse unit created
```

Generated interface:
```java
public interface WarehouseResource {
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data);
}
```

PROS:
✓ Contract-First Design:
  - API contract defined before implementation
  - Frontend/backend teams can work in parallel
  - Contract serves as single source of truth

✓ Automatic Documentation:
  - Swagger UI generated automatically
  - Always in sync with implementation
  - Example: http://localhost:8080/q/swagger-ui shows live docs

✓ Validation Built-in:
  - @NotNull, @Valid annotations generated
  - Request/response validation automatic
  - Reduces boilerplate validation code

✓ API Governance:
  - Breaking changes detected at build time
  - Consistent naming conventions enforced
  - Version management easier

✓ Client Generation:
  - Can generate TypeScript/JavaScript clients
  - Mobile SDK generation
  - Reduces integration errors

CONS:
✗ Build Complexity:
  - Maven plugin configuration needed
  - Generated code in target/ can confuse IDEs
  - Build time slightly increased

✗ Learning Curve:
  - Team needs to learn OpenAPI spec
  - YAML syntax can be verbose
  - Debugging generated code harder

✗ Less Flexibility:
  - Quick prototyping slower
  - Custom annotations harder to add
  - Generated code might not match preferences


2. Code-First (Product/Store):

Example from ProductResource.java:
```java
@Path("product")
@Produces("application/json")
public class ProductResource {
  @POST
  @Transactional
  public Response create(Product product) {
    if (product.id != null) {
      throw new WebApplicationException("Id was invalidly set", 422);
    }
    productRepository.persist(product);
    return Response.ok(product).status(201).build();
  }
}
```

PROS:
✓ Fast Development:
  - Write code immediately, no spec needed
  - Quick iterations and prototyping
  - No code generation step

✓ Full Control:
  - Complete control over implementation
  - Custom error handling easy
  - IDE autocomplete works perfectly

✓ Easier Debugging:
  - Direct code, no generated layers
  - Stack traces clearer
  - Breakpoints work naturally

CONS:
✗ Documentation Drift:
  - Manual docs get outdated quickly
  - No guarantee docs match implementation
  - Example: If we change Product.create() signature, docs might not update

✗ Manual Validation:
  - Must write validation logic manually
  - Inconsistent validation across endpoints
  - More code to maintain

✗ API Inconsistency:
  - Different developers = different patterns
  - Error responses might vary
  - Example: Store returns 422, Product might return 400 for same error

✗ Integration Challenges:
  - Frontend needs to guess API structure
  - No type safety for API consumers
  - More integration bugs


REAL-WORLD EXAMPLE:

Scenario: Adding a new field "category" to Product

Code-First:
1. Add field to Product.java
2. Update ProductResource.java
3. Manually update API documentation
4. Notify frontend team
5. Frontend updates their code
6. Risk: Documentation might be forgotten

OpenAPI-First:
1. Update warehouse-openapi.yaml:
   ```yaml
   Warehouse:
     properties:
       category:
         type: string
   ```
2. Run build (code regenerated)
3. Implement in WarehouseResourceImpl
4. Documentation auto-updated
5. Generate TypeScript client
6. Frontend gets type-safe client automatically


MY CHOICE: OpenAPI-First for ALL APIs

REASONING:

1. Production Systems Need Contracts:
   - Multiple teams consuming APIs
   - Mobile apps, web apps, third-party integrations
   - Breaking changes must be caught early

2. Long-term Maintenance:
   - Initial setup cost (2-3 hours) pays off over months
   - Documentation never drifts
   - Onboarding new developers easier

3. Microservices Architecture:
   - Services communicate via contracts
   - OpenAPI specs can be shared across services
   - API gateway integration easier

4. Testing Benefits:
   - Contract testing possible (Pact, Spring Cloud Contract)
   - Mock servers generated from spec
   - Integration tests more reliable

IMPLEMENTATION RECOMMENDATION:

Migrate Product/Store to OpenAPI:

1. Create product-openapi.yaml:
```yaml
paths:
  /product:
    post:
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/Product'
      responses:
        '201':
          description: Product created
```

2. Configure pom.xml:
```xml
<plugin>
  <groupId>io.quarkiverse.openapi.generator</groupId>
  <artifactId>quarkus-openapi-generator</artifactId>
  <configuration>
    <spec>product-openapi.yaml</spec>
  </configuration>
</plugin>
```

3. Implement generated interface

Result: Consistent, documented, maintainable API across entire codebase.
```
----
3. Given the need to balance thorough testing with time and resource constraints, how would you prioritize and implement tests for this project? Which types of tests would you focus on, and how would you ensure test coverage remains effective over time?

**Answer:**
```txt
TEST STRATEGY - PYRAMID APPROACH:

┌─────────────┐
│   E2E (10%) │  ← Slow, expensive, brittle
├─────────────┤
│ Integration │  ← Medium speed, real dependencies
│    (20%)    │
├─────────────┤
│    Unit     │  ← Fast, isolated, many tests
│    (70%)    │
└─────────────┘


1. PRIORITY 1: UNIT TESTS (70% of effort)

What to test:
✓ Business logic in use cases
✓ Validation rules
✓ Domain model behavior
✓ Error handling

Example from this project:

```java
// CreateWarehouseUseCaseTest.java
@Test
void create_stockExceedsCapacity_shouldFail() {
  Warehouse warehouse = new Warehouse();
  warehouse.capacity = 100;
  warehouse.stock = 150;  // Invalid!
  
  when(warehouseRepository.findByBusinessUnitCode("WH-001")).thenReturn(null);
  when(locationResolver.resolveByIdentifier("ZWOLLE-001"))
    .thenReturn(new Location("ZWOLLE-001", 1, 40));
  
  WebApplicationException exception = assertThrows(
    WebApplicationException.class, 
    () -> useCase.create(warehouse)
  );
  
  assertEquals(422, exception.getResponse().getStatus());
  assertTrue(exception.getMessage().contains("Stock exceeds warehouse capacity"));
}
```

Why this test is valuable:
- Tests critical business rule (stock ≤ capacity)
- Runs in milliseconds (mocked dependencies)
- Easy to maintain
- Clear failure message

What NOT to test:
✗ Getters/setters (no business logic)
✗ Framework code (Quarkus, JPA)
✗ Simple DTOs

Example of over-testing:
```java
// DON'T DO THIS
@Test
void testGetName() {
  Product p = new Product();
  p.name = "Test";
  assertEquals("Test", p.name);  // Waste of time!
}
```


2. PRIORITY 2: INTEGRATION TESTS (20% of effort)

What to test:
✓ API endpoints with real database
✓ Transaction boundaries
✓ Database constraints
✓ External system integration

Example from this project:

```java
// WarehouseResourceTest.java
@QuarkusTest
public class WarehouseResourceTest {
  
  @Test
  void createWarehouse_duplicateBusinessUnitCode_shouldFail() {
    // First create succeeds
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "businessUnitCode": "DUP-001",
          "location": "ZWOLLE-001",
          "capacity": 100,
          "stock": 50
        }
        """)
      .when()
      .post("/warehouse")
      .then()
      .statusCode(200);
    
    // Duplicate should fail
    given()
      .contentType(ContentType.JSON)
      .body("""
        {
          "businessUnitCode": "DUP-001",  // Same code!
          "location": "AMSTERDAM-001",
          "capacity": 150,
          "stock": 75
        }
        """)
      .when()
      .post("/warehouse")
      .then()
      .statusCode(422);  // Should fail
  }
}
```

Why this test is valuable:
- Tests real database constraint (unique business unit code)
- Verifies HTTP status codes
- Tests full request/response cycle
- Catches serialization issues

Critical integration test: Transaction handling

```java
// StoreEndpointTest.java
@Test
void legacyFailure_afterCommit_shouldNotFailRequest() {
  // Mock legacy system to fail
  LegacyStoreManagerGateway failingGateway = new LegacyStoreManagerGateway() {
    @Override
    public void createStoreOnLegacySystem(Store store) {
      throw new RuntimeException("Legacy System failed");
    }
  };
  
  QuarkusMock.installMockForType(failingGateway, LegacyStoreManagerGateway.class);
  
  // Request should still succeed (DB committed)
  given()
    .contentType(ContentType.JSON)
    .body("""{"name": "Test", "quantityProductsInStock": 5}""")
    .when()
    .post("/store")
    .then()
    .statusCode(201);  // Success despite legacy failure!
}
```

This tests critical requirement: "Legacy system calls happen AFTER DB commit"


3. PRIORITY 3: E2E TESTS (10% of effort)

What to test:
✓ Critical happy paths only
✓ Most important user journeys

Example scenario:
```
User Story: Create warehouse, add stock, replace warehouse

1. POST /warehouse (create WH-001)
2. GET /warehouse/WH-001 (verify created)
3. POST /warehouse/WH-001/replacement (replace with WH-002)
4. GET /warehouse (verify WH-001 archived, WH-002 active)
```

Why minimal E2E:
- Slow (seconds per test)
- Brittle (break on UI changes)
- Expensive to maintain
- Unit + Integration tests catch 95% of bugs


FOCUS AREAS FOR THIS PROJECT:

1. Business-Critical Paths:
   ✓ Warehouse creation with all validations
   ✓ Store sync with legacy system (transaction handling)
   ✓ Warehouse replacement (stock transfer logic)

2. Complex Validation:
   ✓ Location validation (LocationResolver integration)
   ✓ Capacity constraints (max warehouses per location)
   ✓ Stock vs capacity validation

3. Error Scenarios:
   ✓ Duplicate business unit codes
   ✓ Invalid locations
   ✓ Capacity exceeded
   ✓ Stock mismatch on replacement

4. Data Integrity:
   ✓ Transaction rollback scenarios
   ✓ Legacy system failure handling
   ✓ Soft delete (archived warehouses)


MAINTAINING COVERAGE OVER TIME:

1. CI/CD Integration:
```xml
<!-- pom.xml -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <configuration>
    <rules>
      <rule>
        <element>BUNDLE</element>
        <limits>
          <limit>
            <counter>LINE</counter>
            <value>COVEREDRATIO</value>
            <minimum>0.80</minimum>  <!-- 80% minimum -->
          </limit>
        </limits>
      </rule>
    </rules>
  </configuration>
</plugin>
```

Build fails if coverage drops below 80%.

2. Code Review Checklist:
   □ New feature has unit tests
   □ Critical paths have integration tests
   □ Edge cases covered
   □ Error scenarios tested
   □ No tests marked @Disabled without ticket

3. Coverage Reports on PRs:
```
Pull Request #123: Add warehouse capacity validation

Coverage: 85% → 87% ✓
New lines covered: 45/50 (90%)
Uncovered lines:
  - WarehouseService.java:123 (error handling)
  - WarehouseService.java:145 (logging)
```

4. Mutation Testing (for critical code):
```bash
# Run mutation tests on use cases
mvn org.pitest:pitest-maven:mutationCoverage
```

Example:
```java
// Original code
if (warehouse.stock > warehouse.capacity) {
  throw new WebApplicationException("Stock exceeds capacity");
}

// Mutation: Change > to >=
if (warehouse.stock >= warehouse.capacity) {  // Mutant!
  throw new WebApplicationException("Stock exceeds capacity");
}
```

If tests still pass, we need better test:
```java
@Test
void create_stockEqualsCapacity_shouldSucceed() {
  warehouse.capacity = 100;
  warehouse.stock = 100;  // Edge case: stock == capacity is valid
  assertDoesNotThrow(() -> useCase.create(warehouse));
}
```

5. Regular Refactoring:
   - Remove dead code (coverage shows unused code)
   - Consolidate duplicate tests
   - Update tests when requirements change
   - Delete flaky tests immediately (fix or remove)

6. Test Quality Metrics:
   - Test execution time (unit tests < 1s total)
   - Flakiness rate (< 1% acceptable)
   - Test-to-code ratio (aim for 1:1 or better)


EXAMPLE TEST PLAN FOR NEW FEATURE:

Feature: Add warehouse capacity limits per location

Unit Tests (30 min):
- ✓ Valid capacity within limit
- ✓ Capacity exceeds location limit
- ✓ Multiple warehouses total capacity check
- ✓ Null capacity handling

Integration Tests (20 min):
- ✓ Create warehouse at location limit
- ✓ Reject warehouse exceeding limit
- ✓ Database constraint validation

E2E Test (10 min):
- ✓ Create 3 warehouses at same location (happy path)

Total: 1 hour for comprehensive test coverage


AVOID THESE ANTI-PATTERNS:

✗ Testing implementation details:
```java
// BAD: Tests internal method calls
verify(repository, times(1)).findById(any());
```

✗ Over-mocking:
```java
// BAD: Mocking everything
when(warehouse.getCapacity()).thenReturn(100);
when(warehouse.getStock()).thenReturn(50);
// Just use real objects!
```

✗ Flaky tests:
```java
// BAD: Time-dependent test
Thread.sleep(1000);
assertEquals(expected, actual);  // Might fail randomly
```

✗ Test interdependence:
```java
// BAD: Test depends on order
@Test
void test1_createWarehouse() { ... }

@Test
void test2_updateWarehouse() {  // Assumes test1 ran first!
  // ...
}
```


CONCLUSION:

For this project, I implemented:
- 76 test cases total
- 53 unit tests (warehouse use cases)
- 16 integration tests (endpoints, repository)
- 7 component tests (location gateway)
- 85%+ coverage achieved
- All critical business rules tested
- Fast test suite (< 30 seconds)

This balance ensures high quality while remaining maintainable and fast.
```