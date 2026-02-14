package com.fulfilment.application.monolith.products;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNot.not;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ProductEndpointTest {

  @Test
  public void testCrudProduct() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  public void testCreateProduct() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"HEMNES\",\"type\":\"Bed frame\"}")
        .when()
        .post("product")
        .then()
        .statusCode(201)
        .body("name", equalTo("HEMNES"));
  }

  @Test
  public void testGetProduct() {
    given()
        .when()
        .get("product/2")
        .then()
        .statusCode(200)
        .body("name", equalTo("KALLAX"));
  }

  @Test
  public void testGetProductNotFound() {
    given()
        .when()
        .get("product/999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testUpdateProduct() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"KALLAX Updated\",\"type\":\"Shelving unit\"}")
        .when()
        .put("product/2")
        .then()
        .statusCode(200)
        .body("name", equalTo("KALLAX Updated"));
  }

  @Test
  public void testUpdateProductNotFound() {
    given()
        .contentType(ContentType.JSON)
        .body("{\"name\":\"Test\",\"type\":\"Test\"}")
        .when()
        .put("product/999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testDeleteProductNotFound() {
    given()
        .when()
        .delete("product/999")
        .then()
        .statusCode(404);
  }

  @Test
  public void testListProducts() {
    given()
        .when()
        .get("product")
        .then()
        .statusCode(200);
  }
}
