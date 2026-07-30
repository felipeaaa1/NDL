package com.ndlcommerce.adapters.persistence.product;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaProductRepository extends JpaRepository<ProductDataMapper, UUID> {

  boolean existsByNameAndActive(String name, boolean active);

  boolean existsByNameAndIdNotAndActive(String name, UUID uuid, boolean active);

  Optional<ProductDataMapper> findByIdAndActive(UUID id, boolean active);

  @Query(
      """
      SELECT p
      FROM ProductDataMapper p
      WHERE p.active = true
        AND LOWER(p.name)
            LIKE LOWER(CONCAT('%', :name, '%'))
        AND LOWER(p.description)
            LIKE LOWER(CONCAT('%', :description, '%'))
        AND (:brandId IS NULL OR p.brandId = :brandId)
        AND (:categoryId IS NULL OR p.categoryId = :categoryId)
      """)
  Slice<ProductDataMapper> findProducts(
      @Param("name") String name,
      @Param("description") String description,
      @Param("brandId") UUID brandId,
      @Param("categoryId") UUID categoryId,
      Pageable pageable);
}
