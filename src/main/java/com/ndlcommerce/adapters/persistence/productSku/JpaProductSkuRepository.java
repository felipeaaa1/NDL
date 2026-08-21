package com.ndlcommerce.adapters.persistence.productSku;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductSkuRepository extends JpaRepository<ProductSkuDataMapper, UUID> {}
