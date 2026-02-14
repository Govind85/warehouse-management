package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return listAll()
            .stream()
            .filter(db -> db.archivedAt == null)
            .peek(System.out::println)
            .map(DbWarehouse::toWarehouse)
            .toList();
  }

  @Override
  public void create(Warehouse warehouse) {

    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = LocalDateTime.now();
    db.archivedAt = null;

    persist(db);
  }

  @Override
  public void update(Warehouse warehouse) {

    DbWarehouse existing =
            find("businessUnitCode", warehouse.businessUnitCode)
                    .firstResult();

    if (existing == null) {
      throw new IllegalArgumentException("Warehouse not found");
    }

    existing.location = warehouse.location;
    existing.capacity = warehouse.capacity;
    existing.stock = warehouse.stock;
    existing.archivedAt = warehouse.archivedAt;
  }

  @Override
  public void remove(Warehouse warehouse) {

    DbWarehouse existing =
            find("businessUnitCode", warehouse.businessUnitCode)
                    .firstResult();

    if (existing != null) {
      existing.archivedAt = LocalDateTime.now(); // soft delete
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {

    DbWarehouse db =
            find("businessUnitCode = ?1", buCode)
                    .stream()
                    .filter(dbWarehouse -> dbWarehouse.archivedAt == null)
                    .findFirst().orElse(null);

    return db != null ? db.toWarehouse() : null;
  }
}
