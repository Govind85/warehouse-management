package com.fulfilment.application.monolith.warehouses.domain.models;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class WarehouseTest {

  @Test
  public void testWarehouse() {
    Warehouse warehouse = new Warehouse();
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime archived = LocalDateTime.now().plusDays(1);
    
    warehouse.setStock(100);
    warehouse.setLocation("ZWOLLE-001");
    warehouse.setCreatedAt(now);
    warehouse.setCapacity(200);
    warehouse.setBusinessUnitCode("BU-001");
    warehouse.setArchivedAt(archived);
    
    assertEquals(100, warehouse.getStock());
    assertEquals("ZWOLLE-001", warehouse.getLocation());
    assertEquals(now, warehouse.getCreatedAt());
    assertEquals(200, warehouse.getCapacity());
    assertEquals("BU-001", warehouse.getBusinessUnitCode());
    assertEquals(archived, warehouse.getArchivedAt());
    
    Warehouse warehouse2 = new Warehouse(150, "AMSTERDAM-001", now, 300, "BU-002", archived);
    assertEquals(150, warehouse2.getStock());
    assertEquals("AMSTERDAM-001", warehouse2.getLocation());
    assertEquals(now, warehouse2.getCreatedAt());
    assertEquals(300, warehouse2.getCapacity());
    assertEquals("BU-002", warehouse2.getBusinessUnitCode());
    assertEquals(archived, warehouse2.getArchivedAt());
  }
}
