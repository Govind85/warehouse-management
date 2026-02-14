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
public class CreateWarehouseUseCaseTest {

  @Inject
  CreateWarehouseUseCase useCase;

  @Test
  void create_duplicateBusinessUnitCode_shouldFail() {
    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        Warehouse existing = new Warehouse();
        existing.businessUnitCode = "WH-001";
        return existing;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Business Unit Code already exists"));
  }

  @Test
  void create_invalidLocation_shouldFail() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.location = "";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return null;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Invalid location"));
  }

  @Test
  void create_locationDoesNotExist_shouldFail() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.location = "INVALID-LOCATION";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return null;
      }
      
      @Override
      public List<Warehouse> getAll() {
        return new ArrayList<>();
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

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Location does not exist"));
  }

  @Test
  void create_stockExceedsCapacity_shouldFail() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "WH-001";
    warehouse.location = "ZWOLLE-001";
    warehouse.capacity = 100;
    warehouse.stock = 150;

    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return null;
      }
      
      @Override
      public List<Warehouse> getAll() {
        return new ArrayList<>();
      }
    };
    
    LocationGateway mockResolver = new LocationGateway() {
      @Override
      public Location resolveByIdentifier(String identifier) {
        return new Location("ZWOLLE-001", 1, 40);
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);
    QuarkusMock.installMockForType(mockResolver, LocationGateway.class);

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.create(warehouse);
    });

    assertEquals(422, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Stock exceeds warehouse capacity"));
  }
}
