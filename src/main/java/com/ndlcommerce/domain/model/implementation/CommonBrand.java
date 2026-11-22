package com.ndlcommerce.domain.model.implementation;

import com.ndlcommerce.domain.model.interfaces.Brand;

public record CommonBrand(String name) implements Brand {

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public boolean nameIsValid() {
    return this.name != null
        && !this.name.isBlank()
        && this.name.length() >= 3
        && this.name.length() <= 50;
  }
}
