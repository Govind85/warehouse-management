package com.fulfilment.application.monolith.stores;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class LegacyStoreManagerGatewayTest {

  @Test
  void createStoreOnLegacySystem_success() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("Test Store");
    store.quantityProductsInStock = 100;

    assertDoesNotThrow(() -> gateway.createStoreOnLegacySystem(store));
  }

  @Test
  void updateStoreOnLegacySystem_success() {
    LegacyStoreManagerGateway gateway = new LegacyStoreManagerGateway();
    Store store = new Store("Updated Store");
    store.quantityProductsInStock = 200;

    assertDoesNotThrow(() -> gateway.updateStoreOnLegacySystem(store));
  }
}
