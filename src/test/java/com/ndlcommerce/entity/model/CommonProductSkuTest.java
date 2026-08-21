package com.ndlcommerce.entity.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.ndlcommerce.entity.model.implementation.CommonProductSku;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommonProductSkuTest {

  @Test
  void shouldAcceptValidSkuCode() {
    var sku = sku("CORRE4-BRANCO-41", new BigDecimal("499.90"));

    assertTrue(sku.skuCodeIsValid());
  }

  @Test
  void shouldRejectMalformedSkuCode() {
    assertFalse(sku("corre4-branco-41", new BigDecimal("499.90")).skuCodeIsValid());
    assertFalse(sku("CORRE4--BRANCO", new BigDecimal("499.90")).skuCodeIsValid());
    assertFalse(sku("CORRE4 BRANCO", new BigDecimal("499.90")).skuCodeIsValid());
  }

  @Test
  void shouldValidatePriceUsingDatabasePrecisionAndScale() {
    assertTrue(sku("SKU-1", new BigDecimal("99999999.99")).priceIsValid());
    assertTrue(sku("SKU-1", new BigDecimal("10.000")).priceIsValid());
    assertFalse(sku("SKU-1", new BigDecimal("99999999.999")).priceIsValid());
    assertFalse(sku("SKU-1", new BigDecimal("100000000.00")).priceIsValid());
    assertFalse(sku("SKU-1", BigDecimal.ZERO).priceIsValid());
  }

  @Test
  void shouldRejectNullAttributeEntries() {
    var attributes = new HashMap<String, String>();
    attributes.put("color", null);
    var sku = new CommonProductSku("SKU-1", attributes, new BigDecimal("10.00"), 1, true);

    assertFalse(sku.attributesAreValid());
  }

  private CommonProductSku sku(String skuCode, BigDecimal price) {
    return new CommonProductSku(skuCode, Map.of("color", "white"), price, 1, true);
  }
}
