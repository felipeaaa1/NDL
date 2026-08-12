package com.ndlcommerce.adapters.persistence.brand;

import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.config.SecurityFilter;
import com.ndlcommerce.useCase.interfaces.brand.BrandRegisterDsGateway;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.*;

public class JpaBrand implements BrandRegisterDsGateway {

  private final JpaBrandRepository repository;
  private final SecurityFilter securityFilter;

  public JpaBrand(JpaBrandRepository repository, SecurityFilter securityFilter) {
    this.repository = repository;
    this.securityFilter = securityFilter;
  }

  @Override
  public boolean existsByName(String name) {
    return repository.existsByNameAndActiveTrue(name);
  }

  @Override
  public BrandGatewayResponseDTO save(BrandDbRequestDTO dbRequest) {
    UserDataMapper userLogado = securityFilter.obterUsuarioLogado();
    BrandDataMapper brandDataMapper = new BrandDataMapper(dbRequest.name(), userLogado.getId());
    return toGatewayResponse(repository.save(brandDataMapper));
  }

  @Override
  public PaginatedResult<BrandGatewayResponseDTO> list(
      BrandDbRequestDTO filter, int page, int size) {
    BrandDataMapper dataMapper = new BrandDataMapper(filter.getName());

    ExampleMatcher matcher =
        ExampleMatcher.matching()
            .withIgnoreCase()
            .withIgnoreNullValues()
            .withIgnorePaths("id", "createdBy", "createdAt", "updatedBy", "updatedAt")
            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

    Example<BrandDataMapper> example = Example.of(dataMapper, matcher);

    Pageable pageable =
        PageRequest.of(page, size, Sort.by(Sort.Order.asc("name"), Sort.Order.asc("id")));
    Page<BrandDataMapper> pageResult = repository.findAll(example, pageable);

    return new PaginatedResult<>(
        pageResult.getContent().stream().map(this::toGatewayResponse).toList(),
        pageResult.getNumber(),
        pageResult.getSize(),
        pageResult.getTotalElements(),
        pageResult.getTotalPages(),
        pageResult.isFirst(),
        pageResult.isLast());
  }

  @Override
  public Optional<BrandGatewayResponseDTO> getById(UUID uuid) {
    return repository.findByIdAndActiveTrue(uuid).map(this::toGatewayResponse);
  }

  @Override
  public boolean existsByNameAndNotId(String name, UUID uuid) {
    return repository.existsByNameAndIdNotAndActiveTrue(name, uuid);
  }

  @Override
  public BrandGatewayResponseDTO update(UUID uuid, BrandDbRequestDTO dbRequestDTO) {
    Optional<BrandDataMapper> optional = repository.findByIdAndActiveTrue(uuid);

    if (optional.isEmpty()) {
      return null;
    }

    BrandDataMapper brandDataMapper = optional.get();
    UserDataMapper userLogado = securityFilter.obterUsuarioLogado();

    brandDataMapper.setName(
        dbRequestDTO.getName().isEmpty() ? brandDataMapper.getName() : dbRequestDTO.getName());
    brandDataMapper.setUpdatedBy(userLogado.getId());

    return toGatewayResponse(repository.save(brandDataMapper));
  }

  @Override
  public void delete(UUID uuid) {
    Optional<BrandDataMapper> optional = repository.findByIdAndActiveTrue(uuid);

    if (optional.isEmpty()) {
      return;
    }

    BrandDataMapper brandDataMapper = optional.get();
    UserDataMapper userLogado = securityFilter.obterUsuarioLogado();

    brandDataMapper.setUpdatedBy(userLogado.getId());
    brandDataMapper.setActive(false);

    repository.save(brandDataMapper);
  }

  private BrandGatewayResponseDTO toGatewayResponse(BrandDataMapper brand) {
    return new BrandGatewayResponseDTO(brand.getId(), brand.getName(), brand.getCreatedAt());
  }
}
