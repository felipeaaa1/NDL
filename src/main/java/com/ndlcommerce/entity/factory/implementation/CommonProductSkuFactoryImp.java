package com.ndlcommerce.entity.factory.implementation;

import com.ndlcommerce.entity.factory.interfaces.ProductSkuFactory;
import com.ndlcommerce.entity.model.implementation.CommonProductSku;
import com.ndlcommerce.entity.model.interfaces.ProductSku;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class CommonProductSkuFactoryImp implements ProductSkuFactory {

  @Override
  public ProductSku create(
      String productName,
      Map<String, String> attributes,
      BigDecimal price,
      int stock,
      boolean active) {
    String skuCode = generateSkuCode(productName, attributes);

    return new CommonProductSku(skuCode, attributes, price, stock, active);
  }

  private static String generateSkuCode(String productName, Map<String, String> attributes) {
    String productPrefix =
        Arrays.stream(productName.trim().split("\\s+"))
            .map(CommonProductSkuFactoryImp::abbreviate)
            .filter(part -> !part.isBlank())
            .collect(Collectors.joining("-"));

    StringBuilder skuCode = new StringBuilder(productPrefix);

    attributes.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .forEach(
            entry ->
                skuCode
                    .append("-")
                    .append(abbreviate(entry.getKey()))
                    .append("-")
                    .append(abbreviate(entry.getValue())));

    return skuCode.toString();
  }

  private static String abbreviate(String value) {
    String normalized =
        Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replaceAll("[^a-zA-Z0-9]", "")
            .toUpperCase(Locale.ROOT);

    return normalized.substring(0, Math.min(3, normalized.length()));
  }
}
