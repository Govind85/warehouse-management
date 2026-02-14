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
public class ArchiveWarehouseUseCaseTest {

  @Inject
  ArchiveWarehouseUseCase useCase;

  @Test
  void archive_warehouseNotFound_shouldFail() {
    WarehouseRepository mockRepo = new WarehouseRepository() {
      @Override
      public Warehouse findByBusinessUnitCode(String buCode) {
        return null;
      }
    };
    
    QuarkusMock.installMockForType(mockRepo, WarehouseRepository.class);

    WebApplicationException exception = assertThrows(WebApplicationException.class, () -> {
      useCase.archive("WH-999");
    });

    assertEquals(404, exception.getResponse().getStatus());
    assertTrue(exception.getMessage().contains("Warehouse not found"));
  }
}
