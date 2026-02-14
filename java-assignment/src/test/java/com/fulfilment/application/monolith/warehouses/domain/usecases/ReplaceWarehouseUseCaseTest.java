package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ReplaceWarehouseUseCaseTest {

  @Inject
  ReplaceWarehouseUseCase useCase;

  @Test
  void replace_warehouseNotFound_shouldFail() {
    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return null;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 150;
    newWarehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(404, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Active warehouse not found"));
  }

  @Test
  void replace_stockMismatch_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 50;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return current;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 150;
    newWarehouse.stock = 75;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Stock must match previous warehouse"));
  }

  @Test
  void replace_capacityCannotAccommodateStock_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 100;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return current;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 100;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("New warehouse capacity cannot accommodate previous stock"));
  }
}
