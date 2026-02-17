package com.fulfilment.application.monolith.fulfillment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
  @Inject CreateFulfillmentUseCase createFulfillmentUseCase;

  private static final Logger LOGGER = Logger.getLogger(FulfillmentResource.class.getName());

  @POST
  @Transactional
  public Response create(FulfillmentRequest request) {
    var fulfillment = createFulfillmentUseCase.create(request.productId, request.storeId, request.warehouseBusinessUnitCode);
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
