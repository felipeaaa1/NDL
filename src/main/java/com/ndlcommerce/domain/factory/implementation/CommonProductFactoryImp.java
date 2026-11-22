package com.ndlcommerce.domain.factory.implementation;

import com.ndlcommerce.domain.factory.interfaces.ProductFactory;
import com.ndlcommerce.domain.model.implementation.CommonProduct;
import com.ndlcommerce.domain.model.interfaces.Product;

public class CommonProductFactoryImp implements ProductFactory {

  @Override
  public Product create(String name, String description) {
    return new CommonProduct(name, description);
  }
}
