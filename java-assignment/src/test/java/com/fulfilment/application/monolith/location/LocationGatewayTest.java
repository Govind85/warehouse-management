package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LocationGatewayTest {

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

    // then
    assertEquals("ZWOLLE-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testWhenResolveLocationShouldReturnBlank() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("ZWOLLE-003");

    // then
    assertEquals("", location.identification);
    assertEquals(0, location.maxNumberOfWarehouses);
    assertEquals(0, location.maxCapacity);
  }

  @Test
  public void testWhenResolveBlankLocationShouldReturn() {
    // given
    LocationGateway locationGateway = new LocationGateway();

    // when
    Location location = locationGateway.resolveByIdentifier("  ");

    // then
    assertEquals("", location.identification);
    assertEquals(0, location.maxNumberOfWarehouses);
    assertEquals(0, location.maxCapacity);
  }

  @Test
  public void testWhenResolveEmptyStringLocationShouldReturnBlank() {
    LocationGateway locationGateway = new LocationGateway();
    Location location = locationGateway.resolveByIdentifier("");
    assertEquals("", location.identification);
    assertEquals(0, location.maxNumberOfWarehouses);
    assertEquals(0, location.maxCapacity);
  }

  @Test
  public void testWhenResolveNullLocationShouldReturnBlank() {
    LocationGateway locationGateway = new LocationGateway();
    Location location = locationGateway.resolveByIdentifier(null);
    assertEquals("", location.identification);
    assertEquals(0, location.maxNumberOfWarehouses);
    assertEquals(0, location.maxCapacity);
  }

  @Test
  public void testResolveAmsterdamLocation() {
    LocationGateway locationGateway = new LocationGateway();
    Location location = locationGateway.resolveByIdentifier("AMSTERDAM-001");
    assertEquals("AMSTERDAM-001", location.identification);
    assertEquals(5, location.maxNumberOfWarehouses);
    assertEquals(100, location.maxCapacity);
  }

  @Test
  public void testResolveTilburgLocation() {
    LocationGateway locationGateway = new LocationGateway();
    Location location = locationGateway.resolveByIdentifier("TILBURG-001");
    assertEquals("TILBURG-001", location.identification);
    assertEquals(1, location.maxNumberOfWarehouses);
    assertEquals(40, location.maxCapacity);
  }

  @Test
  public void testResolveEindhovenLocation() {
    LocationGateway locationGateway = new LocationGateway();
    Location location = locationGateway.resolveByIdentifier("EINDHOVEN-001");
    assertEquals("EINDHOVEN-001", location.identification);
    assertEquals(2, location.maxNumberOfWarehouses);
    assertEquals(70, location.maxCapacity);
  }

  @Test
  public void testResolveAllLocations() {
    LocationGateway locationGateway = new LocationGateway();
    
    Location zwolle2 = locationGateway.resolveByIdentifier("ZWOLLE-002");
    assertEquals("ZWOLLE-002", zwolle2.identification);
    assertEquals(2, zwolle2.maxNumberOfWarehouses);
    assertEquals(50, zwolle2.maxCapacity);
    
    Location amsterdam2 = locationGateway.resolveByIdentifier("AMSTERDAM-002");
    assertEquals("AMSTERDAM-002", amsterdam2.identification);
    assertEquals(3, amsterdam2.maxNumberOfWarehouses);
    assertEquals(75, amsterdam2.maxCapacity);
    
    Location helmond = locationGateway.resolveByIdentifier("HELMOND-001");
    assertEquals("HELMOND-001", helmond.identification);
    assertEquals(1, helmond.maxNumberOfWarehouses);
    assertEquals(45, helmond.maxCapacity);
    
    Location vetsby = locationGateway.resolveByIdentifier("VETSBY-001");
    assertEquals("VETSBY-001", vetsby.identification);
    assertEquals(1, vetsby.maxNumberOfWarehouses);
    assertEquals(90, vetsby.maxCapacity);
  }
  @Test
  void testIdentifierIsNull() {
    LocationGateway locationGateway = new LocationGateway();
    Location result = locationGateway.resolveByIdentifier(null);
    assertEquals("", result.identification);
  }
}
