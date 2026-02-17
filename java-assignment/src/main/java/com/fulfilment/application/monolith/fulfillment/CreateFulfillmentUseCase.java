package com.fulfilment.application.monolith.fulfillment;

import com.fulfilment.application.monolith.products.ProductRepository;
import com.fulfilment.application.monolith.stores.Store;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CreateFulfillmentUseCase {

  @Inject FulfillmentRepository fulfillmentRepository;
  @Inject ProductRepository productRepository;
  @Inject WarehouseRepository warehouseRepository;

  public Fulfillment create(Long productId, Long storeId, String warehouseBusinessUnitCode) {
    if (productRepository.findById(productId) == null) {
      throw new WebApplicationException("Product not found", 404);
    }
    if (Store.findById(storeId) == null) {
      throw new WebApplicationException("Store not found", 404);
    }
    if (warehouseRepository.findByBusinessUnitCode(warehouseBusinessUnitCode) == null) {
      throw new WebApplicationException("Warehouse not found", 404);
    }

    if (fulfillmentRepository.countWarehouseByProductAndStore(productId, storeId) >= 2) {
      throw new WebApplicationException("Product can have max 2 warehouses per store", 400);
    }
    if (fulfillmentRepository.countWarehouseByStore(storeId) >= 3) {
      throw new WebApplicationException("Store can be fulfilled by max 3 warehouses", 400);
    }
    if (fulfillmentRepository.countProductByWarehouse(warehouseBusinessUnitCode) >= 5) {
      throw new WebApplicationException("Warehouse can store max 5 product types", 400);
    }

    var fulfillment = new Fulfillment(productId, storeId, warehouseBusinessUnitCode);
    fulfillmentRepository.persist(fulfillment);
    return fulfillment;
  }
}
