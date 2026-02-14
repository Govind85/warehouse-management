package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final int MAX_WAREHOUSES_PER_LOCATION = 3;
  private static final int MAX_CAPACITY_PER_LOCATION = 1000;

  @Inject
  private WarehouseRepository warehouseRepository;
  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse, String businessUnitCode) {
    var current = warehouseRepository.findByBusinessUnitCode(businessUnitCode);

    if (current == null) {
      throw new WebApplicationException(
              "Active warehouse not found", 404);
    }

    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new WebApplicationException("Invalid location", 422);
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity < current.stock) {
      throw new WebApplicationException(
              "New warehouse capacity cannot accommodate previous stock", 422);
    }

    if (newWarehouse.stock == null || !newWarehouse.stock.equals(current.stock)) {
      throw new WebApplicationException(
              "Stock must match previous warehouse", 422);
    }

    if (newWarehouse.capacity > MAX_CAPACITY_PER_LOCATION) {
      throw new WebApplicationException(
              "Capacity exceeds location maximum", 422);
    }

    // Archive old warehouse
    current.archivedAt = LocalDateTime.now();
    warehouseRepository.update(current);

    // Create new warehouse with SAME business unit code (reuse it)
    newWarehouse.businessUnitCode = businessUnitCode;
    newWarehouse.createdAt = LocalDateTime.now();
    newWarehouse.archivedAt = null;

    warehouseRepository.create(newWarehouse);
  }
}
