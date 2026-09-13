package com.ndlcommerce.useCase.interfaces.productSku;

import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import java.util.Map;

public interface ProductSkuInputBoundary {
  Map<String, Object> list(ProductFilterDTO filter, OrderCursor cursor, int size);
}
