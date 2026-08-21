package com.ndlcommerce.entity.factory.interfaces;

import com.ndlcommerce.entity.model.interfaces.ProductSku;
import java.math.BigDecimal;
import java.util.Map;

public interface ProductSkuFactory {

  ProductSku create(
      String productName,
      Map<String, String> attributes,
      BigDecimal price,
      int stock,
      boolean active);
}
