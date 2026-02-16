package com.fulfilment.application.monolith.fulfillment;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FulfillmentRepository implements PanacheRepository<Fulfillment> {

  public long countWarehouseByProductAndStore(Long productId, Long storeId) {
    return count("productId = ?1 and storeId = ?2", productId, storeId);
  }

  public long countWarehouseByStore(Long storeId) {
    return getEntityManager()
        .createQuery("SELECT COUNT(DISTINCT f.warehouseBusinessUnitCode) FROM Fulfillment f WHERE f.storeId = :storeId", Long.class)
        .setParameter("storeId", storeId)
        .getSingleResult();
  }

  public long countProductByWarehouse(String warehouseBusinessUnitCode) {
    return getEntityManager()
        .createQuery("SELECT COUNT(DISTINCT f.productId) FROM Fulfillment f WHERE f.warehouseBusinessUnitCode = :warehouseCode", Long.class)
        .setParameter("warehouseCode", warehouseBusinessUnitCode)
        .getSingleResult();
  }
}
