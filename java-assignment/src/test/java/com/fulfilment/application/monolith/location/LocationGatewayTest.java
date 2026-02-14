package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
  }

  @Test
  public void testWhenResolveNullLocationShouldReturnBlank() {
    LocationGateway locationGateway = new LocationGateway();
    Location location = locationGateway.resolveByIdentifier(null);
    assertEquals("", location.identification);
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
    assertNotNull(locationGateway.resolveByIdentifier("ZWOLLE-002"));
    assertNotNull(locationGateway.resolveByIdentifier("AMSTERDAM-002"));
    assertNotNull(locationGateway.resolveByIdentifier("HELMOND-001"));
    assertNotNull(locationGateway.resolveByIdentifier("VETSBY-001"));
  }
}
