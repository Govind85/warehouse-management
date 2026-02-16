package com.fulfilment.application.monolith.fulfillment;

import jakarta.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"productId", "storeId", "warehouseBusinessUnitCode"}))
public class Fulfillment {

  @Id @GeneratedValue public Long id;

  @Column(nullable = false)
  public Long productId;

  @Column(nullable = false)
  public Long storeId;

  @Column(nullable = false)
  public String warehouseBusinessUnitCode;

  public Fulfillment() {}

  public Fulfillment(Long productId, Long storeId, String warehouseBusinessUnitCode) {
    this.productId = productId;
    this.storeId = storeId;
    this.warehouseBusinessUnitCode = warehouseBusinessUnitCode;
  }
}
