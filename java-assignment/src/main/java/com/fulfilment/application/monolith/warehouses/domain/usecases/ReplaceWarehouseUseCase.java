package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  @Inject
  private WarehouseRepository warehouseRepository;
  @Inject
  LocationResolver locationResolver;
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

    if (newWarehouse.businessUnitCode != null && !newWarehouse.businessUnitCode.equals(businessUnitCode)) {
      var existing = warehouseRepository.findByBusinessUnitCode(newWarehouse.businessUnitCode);
      if (existing != null) {
        throw new WebApplicationException(
                "Business Unit Code already exists", 422);
      }
    }

    if (newWarehouse.location == null || newWarehouse.location.isBlank()) {
      throw new WebApplicationException("Invalid location", 422);
    }

    var location = locationResolver.resolveByIdentifier(newWarehouse.location);
    if (location.identification == null || location.identification.isEmpty()) {
      throw new WebApplicationException("Location does not exist", 422);
    }

    long warehousesInLocation =
            warehouseRepository.getAll().stream()
                    .filter(w -> w.archivedAt == null)
                    .filter(w -> !w.businessUnitCode.equals(businessUnitCode))
                    .filter(w -> w.location.equals(newWarehouse.location))
                    .count();

    if (warehousesInLocation >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "Maximum warehouses reached for location", 422);
    }

    if (newWarehouse.capacity == null || newWarehouse.capacity <= 0) {
      throw new WebApplicationException(
              "Capacity must be greater than zero", 422);
    }

    if (newWarehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
              "Capacity exceeds location maximum", 422);
    }

    if (newWarehouse.capacity < current.stock) {
      throw new WebApplicationException(
              "New warehouse capacity cannot accommodate previous stock", 422);
    }

    if (newWarehouse.stock == null || newWarehouse.stock < 0) {
      throw new WebApplicationException(
              "Stock must be non-negative", 422);
    }

    if (!newWarehouse.stock.equals(current.stock)) {
      throw new WebApplicationException(
              "Stock must match previous warehouse", 422);
    }

    int totalCapacityInLocation = warehouseRepository.getAll().stream()
            .filter(w -> w.archivedAt == null)
            .filter(w -> !w.businessUnitCode.equals(businessUnitCode))
            .filter(w -> w.location.equals(newWarehouse.location))
            .mapToInt(w -> w.capacity)
            .sum();

    if ((totalCapacityInLocation + newWarehouse.capacity) > location.maxCapacity) {
      throw new WebApplicationException(
              "Total capacity exceeds location maximum", 422);
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
