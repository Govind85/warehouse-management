package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.QuarkusMock;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class ReplaceWarehouseUseCaseTest {

  @Inject
  ReplaceWarehouseUseCase useCase;

  @Test
  void replace_businessUnitCodeAlreadyExists_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 50;

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "WH-002";

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        if ("WH-001".equals(buCode)) return current;
        if ("WH-002".equals(buCode)) return existing;
        return null;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "WH-002";
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Business Unit Code already exists"));
  }

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
      
      @Override
      public List<Warehouse> getAll() {
        return new ArrayList<>();
      }
    };
    
    LocationGateway mockResolver = new LocationGateway() {
      @Override
      public Location resolveByIdentifier(String identifier) {
        return new Location("AMSTERDAM-001", 5, 200);
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);
    QuarkusMock.installMockForType(mockResolver, LocationGateway.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "AMSTERDAM-001";
    newWarehouse.capacity = 80;
    newWarehouse.stock = 75;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
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

  @Test
  void replace_invalidLocation_shouldFail() {
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
    newWarehouse.location = "";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Invalid location"));
  }

  @Test
  void replace_locationDoesNotExist_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 50;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return current;
      }
    };
    
    LocationGateway mockResolver = new LocationGateway() {
      @Override
      public Location resolveByIdentifier(String identifier) {
        return new Location("", 0, 0);
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);
    QuarkusMock.installMockForType(mockResolver, LocationGateway.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "INVALID-LOC";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "INVALID-LOC");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Location does not exist"));
  }

  @Test
  void replace_capacityZero_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 50;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return current;
      }
      
      @Override
      public List<Warehouse> getAll() {
        return new ArrayList<>();
      }
    };
    
    LocationGateway mockResolver = new LocationGateway() {
      @Override
      public Location resolveByIdentifier(String identifier) {
        return new Location("ZWOLLE-001", 1, 100);
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);
    QuarkusMock.installMockForType(mockResolver, LocationGateway.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 0;
    newWarehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Capacity must be greater than zero"));
  }

  @Test
  void replace_negativeStock_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 50;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return current;
      }
      
      @Override
      public List<Warehouse> getAll() {
        return new ArrayList<>();
      }
    };
    
    LocationGateway mockResolver = new LocationGateway() {
      @Override
      public Location resolveByIdentifier(String identifier) {
        return new Location("ZWOLLE-001", 1, 100);
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);
    QuarkusMock.installMockForType(mockResolver, LocationGateway.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = -10;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Stock must be non-negative"));
  }

  @Test
  void replace_totalCapacityExceedsLocationMax_shouldFail() {
    Warehouse current = new Warehouse();
    current.businessUnitCode = "WH-001";
    current.stock = 50;
    current.location = "ZWOLLE-001";

    Warehouse existing = new Warehouse();
    existing.businessUnitCode = "WH-002";
    existing.capacity = 80;
    existing.location = "ZWOLLE-001";
    existing.archivedAt = null;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return current;
      }
      
      @Override
      public List<Warehouse> getAll() {
        List<Warehouse> warehouses = new ArrayList<>();
        warehouses.add(existing);
        return warehouses;
      }
    };
    
    LocationGateway mockResolver = new LocationGateway() {
      @Override
      public Location resolveByIdentifier(String identifier) {
        return new Location("ZWOLLE-001", 3, 100);
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);
    QuarkusMock.installMockForType(mockResolver, LocationGateway.class);

    Warehouse newWarehouse = new Warehouse();
    newWarehouse.location = "ZWOLLE-001";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.replace(newWarehouse, "WH-001");
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Total capacity exceeds location maximum"));
  }
}
