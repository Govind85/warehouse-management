package com.fulfilment.application.monolith.store;

import com.fulfilment.application.monolith.stores.LegacyStoreManagerGateway;
import com.fulfilment.application.monolith.stores.Store;
import io.quarkus.test.junit.QuarkusMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;


@QuarkusTest
public class StoreEndpointTest {
    @BeforeEach
    void resetGateway() {
        // Default: do nothing (success scenario)
        LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway() {
            @Override
            public void createStoreOnLegacySystem(Store store) {
            }

            @Override
            public void updateStoreOnLegacySystem(Store store) {
            }
        };

        QuarkusMock.installMockForType(gateway, LegacyStoreManagerGateway.class);
    }

    @Test
    void createStore_success() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Store A",
                          "quantityProductsInStock": 10
                        }
                        """)
                .when()
                .post("/store")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("name", equalTo("Store A"));
    }

    @Test
    void getAllStores_success() {
        given()
                .when()
                .get("/store")
                .then()
                .statusCode(200);
    }

    @Test
    void updateStore_success() {
        long id =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                        {"name":"Original","quantityProductsInStock":5}
                        """)
                        .when()
                        .post("/store")
                        .then()
                        .extract().jsonPath()
                        .getLong("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Updated","quantityProductsInStock":20}
                        """)
                .when()
                .put("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Updated"));
    }

    @Test
    void deleteStore_success() {
        long id =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                        {"name":"DeleteMe","quantityProductsInStock":3}
                        """)
                        .when()
                        .post("/store")
                        .then()
                        .extract().jsonPath()
                        .getLong("id");

        given()
                .when()
                .delete("/store/" + id)
                .then()
                .statusCode(204);
    }


    @Test
    void createStore_withId_shouldFail() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "id": 1,
                          "name": "Invalid",
                          "quantityProductsInStock": 5
                        }
                        """)
                .when()
                .post("/store")
                .then()
                .statusCode(422)
                .body("exceptionType", containsString("WebApplicationException"));
    }

    @Test
    void updateStore_notFound() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"X","quantityProductsInStock":1}
                        """)
                .when()
                .put("/store/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void updateStore_missingName_shouldFail() {
        long id =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                        {"name":"Temp","quantityProductsInStock":1}
                        """)
                        .when()
                        .post("/store")
                        .then()
                        .extract().jsonPath()
                        .getLong("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"quantityProductsInStock":20}
                        """)
                .when()
                .put("/store/" + id)
                .then()
                .statusCode(422);
    }

    @Test
    void getSingle_notFound() {
        given()
                .when()
                .get("/store/999999")
                .then()
                .statusCode(404);
    }


    @Test
    void legacyFailure_afterCommit_shouldNotFailRequest() {

        LegacyStoreManagerGateway failingGateway = new LegacyStoreManagerGateway() {
            @Override
            public void createStoreOnLegacySystem(Store store) {
                throw new RuntimeException("Legacy System save failed");
            }

            @Override
            public void updateStoreOnLegacySystem(Store store) {
                throw new RuntimeException("Legacy System update failed");
            }
        };

        QuarkusMock.installMockForType(failingGateway, LegacyStoreManagerGateway.class);

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "LegacyFail",
                          "quantityProductsInStock": 5
                        }
                        """)
                .when()
                .post("/store")
                .then()
                .statusCode(201) // Still success because DB committed
                .body("name", equalTo("LegacyFail"));
    }


    @Test
    void errorMapper_shouldReturnJsonStructure() {
        given()
                .contentType(ContentType.JSON)
                .body("""
              {
                "id": 100,
                "name": "Invalid",
                "quantityProductsInStock": 5
              }
              """)
                .when()
                .post("/store")
                .then()
                .statusCode(422)
                .body("code", equalTo(422))
                .body("exceptionType", notNullValue())
                .body("error", notNullValue());
    }

    @Test
    void patchStore_success() {
        long id =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                        {"name":"Original","quantityProductsInStock":5}
                        """)
                        .when()
                        .post("/store")
                        .then()
                        .extract().jsonPath()
                        .getLong("id");

        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"Patched","quantityProductsInStock":30}
                        """)
                .when()
                .patch("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("Patched"));
    }

    @Test
    void patchStore_notFound() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {"name":"X","quantityProductsInStock":1}
                        """)
                .when()
                .patch("/store/99999")
                .then()
                .statusCode(404);
    }

    @Test
    void getSingle_success() {
        long id =
                given()
                        .contentType(ContentType.JSON)
                        .body("""
                        {"name":"GetSingle","quantityProductsInStock":10}
                        """)
                        .when()
                        .post("/store")
                        .then()
                        .extract().jsonPath()
                        .getLong("id");

        given()
                .when()
                .get("/store/" + id)
                .then()
                .statusCode(200)
                .body("name", equalTo("GetSingle"));
    }
}
