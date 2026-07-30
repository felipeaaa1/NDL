package com.ndlcommerce.adapters.persistence.product;

import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.config.SecurityFilter;
import com.ndlcommerce.useCase.interfaces.product.ProductRegisterDsGateway;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductDbRequestDTO;
import com.ndlcommerce.useCase.request.product.ProductUpdateRequestDTO;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class JpaProduct implements ProductRegisterDsGateway {

  private final JpaProductRepository repository;
  private final SecurityFilter securityFilter;

  public JpaProduct(JpaProductRepository repository, SecurityFilter securityFilter) {
    this.repository = repository;
    this.securityFilter = securityFilter;
  }

  @Override
  public boolean existsByName(String name) {
    return repository.existsByNameAndActive(name, true);
  }

  @Override
  public SliceResult<ProductDataMapper> list(
      ProductDbRequestDTO requestDTO, Integer page, Integer size) {

    Sort sort = Sort.by(Sort.Order.desc("createdAt")).and(Sort.by(Sort.Order.desc("id")));

    Pageable pageable = PageRequest.of(page, size, sort);

    String nameFilter = Objects.requireNonNullElse(requestDTO.getName(), "");

    String descriptionFilter = Objects.requireNonNullElse(requestDTO.getDescription(), "");

    Slice<ProductDataMapper> products =
        repository.findProducts(
            nameFilter,
            descriptionFilter,
            requestDTO.getBrand(),
            requestDTO.getCategory(),
            pageable);

    return SliceResult.of(
        products.getContent(), products.getNumber(), products.getSize(), products.hasNext());
  }

  @Override
  public ProductDataMapper save(ProductDbRequestDTO requestDTO) {

    UserDataMapper userLogado = securityFilter.obterUsuarioLogado();

    ProductDataMapper entity =
        new ProductDataMapper(
            requestDTO.getName(),
            requestDTO.getDescription(),
            requestDTO.getBrand(),
            requestDTO.getCategory(),
            userLogado.getId());

    return repository.save(entity);
  }

  @Override
  public Optional<ProductDataMapper> findById(UUID uuid) {
    return repository.findByIdAndActive(uuid, true);
  }

  @Override
  public boolean existsByNameAndIdNot(String name, UUID uuid) {
    return repository.existsByNameAndIdNotAndActive(name, uuid, true);
  }

  @Override
  public ProductDataMapper update(
      ProductDataMapper productDataMapper, ProductUpdateRequestDTO requestDTO) {

    productDataMapper.setName(
        requestDTO.getName() == null ? productDataMapper.getName() : requestDTO.getName());
    productDataMapper.setDescription(
        requestDTO.getDescription() == null
            ? productDataMapper.getDescription()
            : requestDTO.getDescription());
    productDataMapper.setBrandId(
        requestDTO.getBrand() == null ? productDataMapper.getBrandId() : requestDTO.getBrand());
    productDataMapper.setCategoryId(
        requestDTO.getCategory() == null
            ? productDataMapper.getCategoryId()
            : requestDTO.getCategory());
    return repository.save(productDataMapper);
  }

  @Override
  public void delete(UUID productId) {
    ProductDataMapper ProductDataMapper = findById(productId).get();
    ProductDataMapper.setActive(false);
    repository.save(ProductDataMapper);
  }
}
