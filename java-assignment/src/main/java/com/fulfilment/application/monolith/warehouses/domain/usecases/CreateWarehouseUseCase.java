package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {
  private final WarehouseStore warehouseStore;
  @Inject
  WarehouseRepository warehouseRepository;
  @Inject
  LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void create(Warehouse warehouse) {
    var existing = warehouseRepository.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (existing != null) {
      throw new WebApplicationException(
              "Business Unit Code already exists", 422);
    }

    if (warehouse.location == null || warehouse.location.isBlank()) {
      throw new WebApplicationException("Invalid location", 422);
    }

    var location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location.identification == null || location.identification.isEmpty()) {
      throw new WebApplicationException("Location does not exist", 422);
    }

    long warehousesInLocation =
            warehouseRepository.getAll().stream()
                    .filter(w -> w.archivedAt == null)
                    .filter(w -> w.location.equals(warehouse.location))
                    .count();

    if (warehousesInLocation >= location.maxNumberOfWarehouses) {
      throw new WebApplicationException(
              "Maximum warehouses reached for location", 422);
    }

    if (warehouse.capacity == null || warehouse.capacity <= 0) {
      throw new WebApplicationException(
              "Capacity must be greater than zero", 422);
    }

    if (warehouse.capacity > location.maxCapacity) {
      throw new WebApplicationException(
              "Capacity exceeds location maximum", 422);
    }

    int totalCapacityInLocation = warehouseRepository.getAll().stream()
            .filter(w -> w.archivedAt == null)
            .filter(w -> w.location.equals(warehouse.location))
            .mapToInt(w -> w.capacity)
            .sum();

    if ((totalCapacityInLocation + warehouse.capacity) > location.maxCapacity) {
      throw new WebApplicationException(
              "Total capacity exceeds location maximum", 422);
    }

    if (warehouse.stock == null || warehouse.stock < 0
            || warehouse.stock > warehouse.capacity) {
      throw new WebApplicationException(
              "Stock exceeds warehouse capacity", 422);
    }

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;

    warehouseRepository.create(warehouse);
  }
}
