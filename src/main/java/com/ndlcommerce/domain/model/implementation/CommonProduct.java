package com.ndlcommerce.domain.model.implementation;

import com.ndlcommerce.domain.model.interfaces.Product;

public class CommonProduct implements Product {

  private final String name;
  private final String description;

  public CommonProduct(String name, String description) {
    this.name = name;
    this.description = description;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public String getDescription() {
    return this.description;
  }

  @Override
  public boolean nameIsValid() {
    return this.name != null
        && !this.name.isBlank()
        && this.name.length() >= 3
        && this.name.length() < 200;
  }

  @Override
  public boolean descriptionIsValid() {
    return this.description != null
        && this.description.length() > 5
        && this.description.length() <= 500;
  }
}
