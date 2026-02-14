package com.fulfilment.application.monolith.products;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

@QuarkusTest
public class ErrorMapperTest {

  @Inject ObjectMapper objectMapper;

  @Test
  public void testErrorMapperWithWebApplicationException() {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    mapper.objectMapper = objectMapper;

    WebApplicationException exception = new WebApplicationException("Test error", 404);
    Response response = mapper.toResponse(exception);

    assertEquals(404, response.getStatus());
    ObjectNode entity = (ObjectNode) response.getEntity();
    assertEquals(404, entity.get("code").asInt());
    assertEquals("Test error", entity.get("error").asText());
    assertNotNull(entity.get("exceptionType"));
  }

  @Test
  public void testErrorMapperWithGenericException() {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    mapper.objectMapper = objectMapper;

    Exception exception = new RuntimeException("Generic error");
    Response response = mapper.toResponse(exception);

    assertEquals(500, response.getStatus());
    ObjectNode entity = (ObjectNode) response.getEntity();
    assertEquals(500, entity.get("code").asInt());
    assertEquals("Generic error", entity.get("error").asText());
    assertNotNull(entity.get("exceptionType"));
  }

  @Test
  public void testErrorMapperWithExceptionWithoutMessage() {
    ProductResource.ErrorMapper mapper = new ProductResource.ErrorMapper();
    mapper.objectMapper = objectMapper;

    Exception exception = new RuntimeException();
    Response response = mapper.toResponse(exception);

    assertEquals(500, response.getStatus());
    ObjectNode entity = (ObjectNode) response.getEntity();
    assertEquals(500, entity.get("code").asInt());
    assertNotNull(entity.get("exceptionType"));
  }
}
