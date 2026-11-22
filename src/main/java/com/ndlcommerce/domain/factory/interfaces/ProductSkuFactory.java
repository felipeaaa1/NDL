package com.ndlcommerce.domain.factory.interfaces;

import com.ndlcommerce.domain.model.interfaces.ProductSku;
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
