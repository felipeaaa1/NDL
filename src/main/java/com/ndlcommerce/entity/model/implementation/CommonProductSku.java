package com.ndlcommerce.entity.model.implementation;

import com.ndlcommerce.entity.model.interfaces.ProductSku;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CommonProductSku implements ProductSku {

  private static final int MAX_SKU_CODE_LENGTH = 100;
  private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");
  private static final Pattern SKU_CODE_PATTERN = Pattern.compile("[A-Z0-9]+(?:-[A-Z0-9]+)*");

  private final String skuCode;
  private final Map<String, String> attributes;
  private final BigDecimal price;
  private final int stock;
  private final boolean active;

  public CommonProductSku(
      String skuCode, Map<String, String> attributes, BigDecimal price, int stock, boolean active) {
    this.skuCode = skuCode;
    this.attributes =
        attributes == null ? Map.of() : Collections.unmodifiableMap(new HashMap<>(attributes));
    this.price = price;
    this.stock = stock;
    this.active = active;
  }

  @Override
  public String getSkuCode() {
    return skuCode;
  }

  @Override
  public Map<String, String> getAttributes() {
    return attributes;
  }

  @Override
  public BigDecimal getPrice() {
    return price;
  }

  @Override
  public int getStock() {
    return stock;
  }

  @Override
  public boolean isActive() {
    return active;
  }

  @Override
  public boolean skuCodeIsValid() {
    return skuCode != null
        && !skuCode.isBlank()
        && skuCode.length() <= MAX_SKU_CODE_LENGTH
        && SKU_CODE_PATTERN.matcher(skuCode).matches();
  }

  @Override
  public boolean attributesAreValid() {
    return attributes.entrySet().stream()
        .allMatch(
            entry ->
                entry.getKey() != null
                    && !entry.getKey().isBlank()
                    && entry.getValue() != null
                    && !entry.getValue().isBlank());
  }

  @Override
  public boolean priceIsValid() {
    return price != null
        && price.compareTo(BigDecimal.ZERO) > 0
        && price.compareTo(MAX_PRICE) <= 0
        && price.stripTrailingZeros().scale() <= 2;
  }

  @Override
  public boolean stockIsValid() {
    return stock >= 0;
  }

  @Override
  public boolean hasStock() {
    return stock > 0;
  }

  @Override
  public boolean isValid() {
    return skuCodeIsValid() && attributesAreValid() && priceIsValid() && stockIsValid();
  }
}
