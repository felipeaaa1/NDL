package com.ndlcommerce.domain.factory.interfaces;

import com.ndlcommerce.domain.model.interfaces.Product;

public interface ProductFactory {

  Product create(String name, String description);
}
