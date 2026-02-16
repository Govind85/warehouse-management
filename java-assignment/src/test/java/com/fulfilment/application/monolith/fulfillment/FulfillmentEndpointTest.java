package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class FulfillmentEndpointTest {

  @Test
  @Order(1)
  public void testCreateFulfillment() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":1,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.001\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);
  }

  @Test
  @Order(2)
  public void testListFulfillments() {
    given()
        .when().get("fulfillments")
        .then()
        .statusCode(200);
  }

  @Test
  @Order(3)
  public void testDeleteFulfillment() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":2,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .when().delete("fulfillments/2")
        .then()
        .statusCode(204);
  }

  @Test
  @Order(4)
  public void testDeleteNonExistentFulfillment() {
    given()
        .when().delete("fulfillments/999")
        .then()
        .statusCode(404);
  }

  @Test
  @Order(5)
  public void testCreateFulfillmentWithInvalidProduct() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":999,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.001\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(404)
        .body("error", equalTo("Product not found"));
  }

  @Test
  @Order(6)
  public void testCreateFulfillmentWithInvalidStore() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":1,\"storeId\":999,\"warehouseBusinessUnitCode\":\"MWH.001\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(404)
        .body("error", equalTo("Store not found"));
  }

  @Test
  @Order(7)
  public void testCreateFulfillmentWithInvalidWarehouse() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":1,\"storeId\":1,\"warehouseBusinessUnitCode\":\"INVALID\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(404)
        .body("error", equalTo("Warehouse not found"));
  }

  @Test
  @Order(8)
  public void testMaxTwoWarehousesPerProductPerStore() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":3,\"storeId\":3,\"warehouseBusinessUnitCode\":\"MWH.001\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":3,\"storeId\":3,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":3,\"storeId\":3,\"warehouseBusinessUnitCode\":\"MWH.023\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(400)
        .body("error", equalTo("Product can have max 2 warehouses per store"));
  }

  @Test
  @Order(9)
  public void testMaxThreeWarehousesPerStore() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":1,\"storeId\":2,\"warehouseBusinessUnitCode\":\"MWH.001\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":2,\"storeId\":2,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":3,\"storeId\":2,\"warehouseBusinessUnitCode\":\"MWH.023\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":1,\"storeId\":2,\"warehouseBusinessUnitCode\":\"MWH.001\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(400)
        .body("error", equalTo("Store can be fulfilled by max 3 warehouses"));
  }

  @Test
  @Order(10)
  public void testMaxFiveProductsPerWarehouse() {
    // Add 5 distinct products to MWH.012
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":1,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":2,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":3,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":4,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":5,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(201);

    // Try to add a 6th product - should fail
    given()
        .contentType(ContentType.JSON)
        .body("{\"productId\":6,\"storeId\":1,\"warehouseBusinessUnitCode\":\"MWH.012\"}")
        .when().post("fulfillments")
        .then()
        .statusCode(400)
        .body("error", equalTo("Warehouse can store max 5 product types"));
  }
}
