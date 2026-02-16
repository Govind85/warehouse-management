package com.fulfilment.application.monolith.fulfillment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.List;
import org.jboss.logging.Logger;

@Path("/fulfillments")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FulfillmentResource {

  @Inject FulfillmentRepository fulfillmentRepository;
  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  private static final Logger LOGGER = Logger.getLogger(FulfillmentResource.class.getName());

  @POST
  @Transactional
  public Response create(FulfillmentRequest request) {
    if (productRepository.findById(request.productId) == null) {
      throw new WebApplicationException("Product not found", 404);
    }
    if (Store.findById(request.storeId) == null) {
      throw new WebApplicationException("Store not found", 404);
    }
    if (warehouseRepository.findByBusinessUnitCode(request.warehouseBusinessUnitCode) == null) {
      throw new WebApplicationException("Warehouse not found", 404);
    }

    if (fulfillmentRepository.countWarehouseByProductAndStore(request.productId, request.storeId) >= 2) {
      throw new WebApplicationException("Product can have max 2 warehouses per store", 400);
    }
    if (fulfillmentRepository.countWarehouseByStore(request.storeId) >= 3) {
      throw new WebApplicationException("Store can be fulfilled by max 3 warehouses", 400);
    }
    if (fulfillmentRepository.countProductByWarehouse(request.warehouseBusinessUnitCode) >= 5) {
      throw new WebApplicationException("Warehouse can store max 5 product types", 400);
    }

    var fulfillment = new Fulfillment(request.productId, request.storeId, request.warehouseBusinessUnitCode);
    fulfillmentRepository.persist(fulfillment);
    return Response.status(201).entity(fulfillment).build();
  }

  @GET
  public List<Fulfillment> list() {
    return fulfillmentRepository.listAll();
  }

  @GET
  @Path("{productId}/fulfillment")
  public List<Fulfillment> getFulfillments(@PathParam("productId") Long productId) {
    return fulfillmentRepository.list("productId", productId);
  }

  @DELETE
  @Path("/{id}")
  @Transactional
  public Response delete(@PathParam("id") Long id) {
    boolean deleted = fulfillmentRepository.deleteById(id);
    if (!deleted) {
      throw new WebApplicationException("Fulfillment with id of " + id + " does not exist.", 404);
    }
    return Response.status(204).build();
  }

  @Provider
  public static class ErrorMapper implements ExceptionMapper<Exception> {

    @Inject ObjectMapper objectMapper;

    @Override
    public Response toResponse(Exception exception) {
      LOGGER.error("Failed to handle request", exception);

      int code = 500;
      if (exception instanceof WebApplicationException) {
        code = ((WebApplicationException) exception).getResponse().getStatus();
      }

      ObjectNode exceptionJson = objectMapper.createObjectNode();
      exceptionJson.put("exceptionType", exception.getClass().getName());
      exceptionJson.put("code", code);

      if (exception.getMessage() != null) {
        exceptionJson.put("error", exception.getMessage());
      }

      return Response.status(code).entity(exceptionJson).build();
    }
  }

  public static class FulfillmentRequest {
    public Long productId;
    public Long storeId;
    public String warehouseBusinessUnitCode;
  }
}
