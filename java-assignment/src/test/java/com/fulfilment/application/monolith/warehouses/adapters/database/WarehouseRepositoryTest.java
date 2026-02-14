package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class WarehouseRepositoryTest {

  @Inject
  WarehouseRepository repository;

  @Test
  @Transactional
  void create_success() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "REPO-TEST-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    repository.create(warehouse);

    Warehouse found = repository.findByBusinessUnitCode("REPO-TEST-001");
    assertNotNull(found);
    assertEquals("REPO-TEST-001", found.businessUnitCode);
    assertEquals("ZWOLLE-001", found.location);
    assertEquals(100, found.capacity);
    assertEquals(50, found.stock);
  }

  @Test
  @Transactional
  void update_success() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "REPO-UPDATE-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    repository.create(warehouse);

    Warehouse found = repository.findByBusinessUnitCode("REPO-UPDATE-001");
    found.stock = 75;
    found.capacity = 150;

    repository.update(found);

    Warehouse updated = repository.findByBusinessUnitCode("REPO-UPDATE-001");
    assertEquals(75, updated.stock);
    assertEquals(150, updated.capacity);
  }

  @Test
  @Transactional
  void update_notFound_shouldThrowException() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "NON-EXISTENT";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    assertThrows(IllegalArgumentException.class, () -> {
      repository.update(warehouse);
    });
  }

  @Test
  @Transactional
  void remove_success() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "REPO-REMOVE-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    repository.create(warehouse);

    Warehouse found = repository.findByBusinessUnitCode("REPO-REMOVE-001");
    assertNotNull(found);

    repository.remove(found);

    Warehouse removed = repository.findByBusinessUnitCode("REPO-REMOVE-001");
    assertNull(removed);
  }

  @Test
  @Transactional
  void findByBusinessUnitCode_notFound_shouldReturnNull() {
    Warehouse found = repository.findByBusinessUnitCode("NON-EXISTENT-CODE");
    assertNull(found);
  }

  @Test
  @Transactional
  void findByBusinessUnitCode_archived_shouldReturnNull() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "ARCHIVED-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    repository.create(warehouse);

    Warehouse found = repository.findByBusinessUnitCode("ARCHIVED-001");
    found.archivedAt = LocalDateTime.now();
    repository.update(found);

    Warehouse archived = repository.findByBusinessUnitCode("ARCHIVED-001");
    assertNull(archived);
  }

  @Test
  @Transactional
  void getAll_success() {
    Warehouse warehouse1 = new Warehouse();
    warehouse1.businessUnitCode = "GET-ALL-001";
    warehouse1.location = "ZWOLLE-001";
    warehouse1.capacity = 100;
    warehouse1.stock = 50;

    Warehouse warehouse2 = new Warehouse();
    warehouse2.businessUnitCode = "GET-ALL-002";
    warehouse2.location = "AMSTERDAM-001";
    warehouse2.capacity = 150;
    warehouse2.stock = 75;

    repository.create(warehouse1);
    repository.create(warehouse2);

    List<Warehouse> all = repository.getAll();
    assertTrue(all.size() >= 2);
    assertTrue(all.stream().anyMatch(w -> "GET-ALL-001".equals(w.businessUnitCode)));
    assertTrue(all.stream().anyMatch(w -> "GET-ALL-002".equals(w.businessUnitCode)));
  }
}
