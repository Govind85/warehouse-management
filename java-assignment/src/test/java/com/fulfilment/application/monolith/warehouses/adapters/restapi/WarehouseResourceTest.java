package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
@TestMethodOrder(MethodOrderer.MethodName.class)
public class WarehouseResourceTest {

  @Test
  void listAllWarehouses_success() {
    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body("$", notNullValue());
  }

  @Test
  void createWarehouse_success() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "businessUnitCode": "TEST-WH-001",
              "location": "AMSTERDAM-001",
              "capacity": 100,
              "stock": 50
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo("TEST-WH-001"))
        .body("location", equalTo("AMSTERDAM-001"))
        .body("capacity", equalTo(100))
        .body("stock", equalTo(50));
  }

  @Test
  void createWarehouse_duplicateBusinessUnitCode_shouldFail() {
    String businessUnitCode = "DUP-WH-" + System.currentTimeMillis();
    
    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "AMSTERDAM-002",
              "capacity": 100,
              "stock": 50
            }
            """, businessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "TILBURG-001",
              "capacity": 150,
              "stock": 75
            }
            """, businessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(422);
  }

  @Test
  void createWarehouse_invalidLocation_shouldFail() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "businessUnitCode": "INVALID-LOC-001",
              "location": "",
              "capacity": 100,
              "stock": 50
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(422);
  }

  @Test
  void createWarehouse_stockExceedsCapacity_shouldFail() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "businessUnitCode": "STOCK-EXCEED-001",
              "location": "VETSBY-001",
              "capacity": 100,
              "stock": 150
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(422);
  }

  @Test
  void createWarehouse_capacityExceedsMaximum_shouldFail() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "businessUnitCode": "CAP-EXCEED-001",
              "location": "ZWOLLE-001",
              "capacity": 1001,
              "stock": 50
            }
            """)
        .when()
        .post("/warehouse")
        .then()
        .statusCode(422);
  }

  @Test
  void getWarehouseById_success() {
    String businessUnitCode = "GET-WH-" + System.currentTimeMillis();
    
    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "EINDHOVEN-001",
              "capacity": 100,
              "stock": 50
            }
            """, businessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .when()
        .get("/warehouse/" + businessUnitCode)
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo(businessUnitCode));
  }

  @Test
  void getWarehouseById_notFound_shouldFail() {
    given()
        .when()
        .get("/warehouse/NON-EXISTENT-999")
        .then()
        .statusCode(404);
  }

  @Test
  void archiveWarehouse_success() {
    String businessUnitCode = "ARCHIVE-WH-" + System.currentTimeMillis();
    
    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "HELMOND-001",
              "capacity": 100,
              "stock": 50
            }
            """, businessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .when()
        .delete("/warehouse/" + businessUnitCode)
        .then()
        .statusCode(204);

    given()
        .when()
        .get("/warehouse")
        .then()
        .statusCode(200)
        .body("businessUnitCode", not(hasItem(businessUnitCode)));
  }

  @Test
  void archiveWarehouse_notFound_shouldFail() {
    given()
        .when()
        .delete("/warehouse/NON-EXISTENT-999")
        .then()
        .statusCode(404);
  }

  @Test
  void replaceWarehouse_success() {
    String oldBusinessUnitCode = "OLD-WH-" + System.currentTimeMillis();
    
    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "TILBURG-001",
              "capacity": 100,
              "stock": 50
            }
            """, oldBusinessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "location": "VETSBY-001",
              "capacity": 150,
              "stock": 50
            }
            """)
        .when()
        .post("/warehouse/" + oldBusinessUnitCode + "/replacement")
        .then()
        .statusCode(200)
        .body("businessUnitCode", equalTo(oldBusinessUnitCode));  // Same code reused!
  }

  @Test
  void replaceWarehouse_stockMismatch_shouldFail() {
    String oldBusinessUnitCode = "STOCK-MISMATCH-" + System.currentTimeMillis();
    
    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "ZWOLLE-002",
              "capacity": 100,
              "stock": 50
            }
            """, oldBusinessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "location": "HELMOND-001",
              "capacity": 150,
              "stock": 75
            }
            """)
        .when()
        .post("/warehouse/" + oldBusinessUnitCode + "/replacement")
        .then()
        .statusCode(422);
  }

  @Test
  void replaceWarehouse_capacityCannotAccommodateStock_shouldFail() {
    String oldBusinessUnitCode = "CAP-FAIL-" + System.currentTimeMillis();
    
    given()
        .contentType(ContentType.JSON)
        .body(String.format("""
            {
              "businessUnitCode": "%s",
              "location": "ZWOLLE-002",
              "capacity": 100,
              "stock": 80
            }
            """, oldBusinessUnitCode))
        .when()
        .post("/warehouse")
        .then()
        .statusCode(200);

    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "location": "AMSTERDAM-001",
              "capacity": 50,
              "stock": 80
            }
            """)
        .when()
        .post("/warehouse/" + oldBusinessUnitCode + "/replacement")
        .then()
        .statusCode(422);
  }

  @Test
  void replaceWarehouse_notFound_shouldFail() {
    given()
        .contentType(ContentType.JSON)
        .body("""
            {
              "businessUnitCode": "NEW-WH-001",
              "location": "EINDHOVEN-001",
              "capacity": 150,
              "stock": 50
            }
            """)
        .when()
        .post("/warehouse/NON-EXISTENT-999/replacement")
        .then()
        .statusCode(404);
  }
}
