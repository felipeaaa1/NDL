package com.ndlcommerce.useCase;

import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuInputBoundary;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuRegisterDsGateway;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;

public class ProductSkuInteractor implements ProductSkuInputBoundary {
  private final ProductSkuRegisterDsGateway productSkuDsGateway;

  public ProductSkuInteractor(ProductSkuRegisterDsGateway productSkuDsGateway) {
    this.productSkuDsGateway = productSkuDsGateway;
  }

  @Override
  public SliceResult<ProductSkuResponseDTO> list(ProductFilterDTO filter, int page, int size) {
    ProductSkuDbRequestDTO request =
        new ProductSkuDbRequestDTO(
            filter != null ? filter.getName() : null,
            filter != null ? filter.getDescription() : null,
            filter != null ? filter.getBrand() : null,
            filter != null ? filter.getCategory() : null);

    return productSkuDsGateway.list(request, page, size);
  }
}
