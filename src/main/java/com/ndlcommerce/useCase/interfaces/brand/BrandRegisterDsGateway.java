package com.ndlcommerce.useCase.interfaces.brand;

import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import java.util.Optional;
import java.util.UUID;

public interface BrandRegisterDsGateway {

  boolean existsByName(String name);

  BrandGatewayResponseDTO save(BrandDbRequestDTO dbRequest);

  PaginatedResult<BrandGatewayResponseDTO> list(BrandDbRequestDTO filter, int page, int size);

  Optional<BrandGatewayResponseDTO> getById(UUID uuid);

  boolean existsByNameAndNotId(String name, UUID uuid);

  BrandGatewayResponseDTO update(UUID uuid, BrandDbRequestDTO dbRequestDTO);

  void delete(UUID uuid);
}
