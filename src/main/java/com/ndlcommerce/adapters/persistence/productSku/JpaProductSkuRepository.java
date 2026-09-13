package com.ndlcommerce.adapters.persistence.productSku;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaProductSkuRepository extends JpaRepository<ProductSkuDataMapper, UUID> {
  boolean existsBySkuCodeIn(Collection<String> skuCode);

  @Query(
      """
      SELECT productsku.id AS skuId,
             product.id AS productId,
             product.name AS name,
             productsku.price AS price,
             productsku.stock AS stock,
             productsku.createdAt AS createdAt
      FROM ProductSkuDataMapper productsku
      INNER JOIN ProductDataMapper product
          ON product.id = productsku.productId
      WHERE product.active = true
        AND productsku.active = true
        AND (
            :firstPage = true
            OR productsku.createdAt > :createdAt
            OR (
                productsku.createdAt = :createdAt
                AND productsku.id < :skuId
            )
        )
        AND LOWER(product.name) LIKE LOWER(CONCAT('%', :name, '%'))
        AND LOWER(product.description) LIKE LOWER(CONCAT('%', :description, '%'))
        AND (:brandId IS NULL OR product.brandId = :brandId)
        AND (:categoryId IS NULL OR product.categoryId = :categoryId)
      ORDER BY productsku.createdAt ASC, productsku.id DESC
            """)
  Slice<PublicProductSkuView> findPublicSkus(
      @Param("name") String name,
      @Param("description") String description,
      @Param("firstPage") boolean firstPage,
      @Param("createdAt") LocalDateTime createdAt,
      @Param("skuId") UUID skuId,
      @Param("brandId") UUID brandId,
      @Param("categoryId") UUID categoryId,
      Pageable pageable);

  interface PublicProductSkuView {
    UUID getSkuId();

    UUID getProductId();

    String getName();

    BigDecimal getPrice();

    LocalDateTime getCreatedAt();

    Integer getStock();
  }
}
