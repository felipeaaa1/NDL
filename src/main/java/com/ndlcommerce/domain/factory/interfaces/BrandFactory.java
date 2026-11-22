package com.ndlcommerce.domain.factory.interfaces;

import com.ndlcommerce.domain.model.interfaces.Brand;

public interface BrandFactory {
  Brand create(String name);
}
