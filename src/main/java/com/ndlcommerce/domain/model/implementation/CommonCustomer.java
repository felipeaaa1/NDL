package com.ndlcommerce.domain.model.implementation;

import com.ndlcommerce.domain.model.interfaces.Customer;
import java.util.UUID;

public class CommonCustomer implements Customer {

  private final UUID userId;
  private final String name;
  private final String contact;
  private final String address;

  public CommonCustomer(UUID userId, String name, String contact, String address) {
    this.userId = userId;
    this.name = name;
    this.contact = contact;
    this.address = address;
  }

  @Override
  public boolean nameIsValid() {
    return this.name != null
        && !this.name.isBlank()
        && this.name.length() >= 4
        && this.name.length() <= 50;
  }

  @Override
  public boolean hasAddress() {
    return this.address != null && !this.address.isBlank();
  }

  @Override
  public boolean isCustumerActive() {
    return false;
  }

  @Override
  public UUID getUserId() {
    return this.userId;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getContact() {
    return this.contact;
  }

  @Override
  public String getAddress() {
    return this.address;
  }
}
