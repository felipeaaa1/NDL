package com.ndlcommerce.useCase.interfaces.product;

import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductDbRequestDTO;
import com.ndlcommerce.useCase.request.product.ProductResponseDTO;
import com.ndlcommerce.useCase.request.product.ProductUpdateRequestDTO;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface ProductRegisterDsGateway {

  boolean existsByName(String name);

  SliceResult<ProductResponseDTO> list(ProductDbRequestDTO requestDTO, Integer page, Integer size);

  ProductResponseDTO save(ProductDbRequestDTO requestDTO);

  Optional<ProductResponseDTO> findById(UUID uuid);

  boolean existsByNameAndIdNot(String name, UUID uuid);

  ProductResponseDTO update(UUID productDataMapperId, ProductUpdateRequestDTO requestDTO);

  void delete(UUID productId);

  boolean skuCodesExist(Set<String> uniqueSkuCodes);
}
