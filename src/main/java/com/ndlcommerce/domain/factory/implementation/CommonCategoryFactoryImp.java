package com.ndlcommerce.domain.factory.implementation;

import com.ndlcommerce.domain.factory.interfaces.CategoryFactory;
import com.ndlcommerce.domain.model.implementation.CommonCategory;
import com.ndlcommerce.domain.model.interfaces.Category;

public class CommonCategoryFactoryImp implements CategoryFactory {

  @Override
  public Category create(String name) {
    return new CommonCategory(name);
  }
}
