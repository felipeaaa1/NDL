package com.ndlcommerce.entity.model.interfaces;

import java.math.BigDecimal;
import java.util.Map;

public interface ProductSku {

  String getSkuCode();

  Map<String, String> getAttributes();

  BigDecimal getPrice();

  int getStock();

  boolean isActive();

  boolean skuCodeIsValid();

  boolean attributesAreValid();

  boolean priceIsValid();

  boolean stockIsValid();

  boolean hasStock();

  boolean isValid();
}
