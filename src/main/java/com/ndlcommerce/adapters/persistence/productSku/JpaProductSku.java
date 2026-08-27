package com.ndlcommerce.adapters.persistence.productSku;

import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuRegisterDsGateway;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Objects;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class JpaProductSku implements ProductSkuRegisterDsGateway {

  private final JpaProductSkuRepository repository;

  public JpaProductSku(JpaProductSkuRepository repository) {
    this.repository = repository;
  }

  @Override
  public SliceResult<ProductSkuResponseDTO> list(
      ProductSkuDbRequestDTO requestDTO, Integer page, Integer size) {
    var sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    var pageable = PageRequest.of(page, size, sort);
    var result =
        repository.findPublicSkus(
            Objects.requireNonNullElse(requestDTO.name(), ""),
            Objects.requireNonNullElse(requestDTO.description(), ""),
            requestDTO.brand(),
            requestDTO.category(),
            pageable);

    return SliceResult.of(
            result.getContent(), result.getNumber(), result.getSize(), result.hasNext())
        .map(
            row ->
                new ProductSkuResponseDTO(
                    row.getSkuId(),
                    row.getProductId(),
                    row.getName(),
                    slugify(row.getName()),
                    row.getPrice(),
                    null,
                    row.getStock() > 0));
  }

  private String slugify(String name) {
    return Normalizer.normalize(name, Normalizer.Form.NFD)
        .replaceAll("\\p{M}", "")
        .toLowerCase(Locale.ROOT)
        .replaceAll("[^a-z0-9]+", "-")
        .replaceAll("(^-|-$)", "");
  }
}
