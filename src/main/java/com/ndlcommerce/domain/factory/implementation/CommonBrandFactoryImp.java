package com.ndlcommerce.domain.factory.implementation;

import com.ndlcommerce.domain.factory.interfaces.BrandFactory;
import com.ndlcommerce.domain.model.implementation.CommonBrand;
import com.ndlcommerce.domain.model.interfaces.Brand;

public class CommonBrandFactoryImp implements BrandFactory {

  @Override
  public Brand create(String name) {
    return new CommonBrand(name);
  }
}
