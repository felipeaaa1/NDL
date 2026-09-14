package com.ndlcommerce.entity.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.ndlcommerce.entity.factory.implementation.CommonProductSkuFactoryImp;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import org.junit.jupiter.api.Test;

class CommonProductSkuFactoryImpTest {

  private final CommonProductSkuFactoryImp factory = new CommonProductSkuFactoryImp();

  @Test
  void shouldGenerateNormalizedSkuCodeWithDeterministicAttributeOrder() {
    var attributes = new LinkedHashMap<String, String>();
    attributes.put("tamanho", "41");
    attributes.put("cor", "bêge");

    var sku = factory.create("Tênis Corre 5", attributes, new BigDecimal("499.90"), 10, true);

    assertEquals("TEN-COR-5-COR-BEG-TAM-41", sku.getSkuCode());
  }
}
