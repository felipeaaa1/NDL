package com.ndlcommerce.domain.factory.interfaces;

import com.ndlcommerce.domain.model.interfaces.Category;

public interface CategoryFactory {
  Category create(String name);
}
