package com.ndlcommerce.useCase;

import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuInputBoundary;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuPresenter;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuRegisterDsGateway;
import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;
import java.util.Map;

public class ProductSkuInteractor implements ProductSkuInputBoundary {
  private final ProductSkuRegisterDsGateway productSkuDsGateway;
  private final ProductSkuPresenter productSkuPresenter;

  public ProductSkuInteractor(
      ProductSkuRegisterDsGateway productSkuDsGateway, ProductSkuPresenter productSkuPresenter) {
    this.productSkuDsGateway = productSkuDsGateway;
    this.productSkuPresenter = productSkuPresenter;
  }

  @Override
  public Map<String, Object> list(ProductFilterDTO filter, OrderCursor cursor, int size) {
    ProductSkuDbRequestDTO request =
        new ProductSkuDbRequestDTO(
            filter != null ? filter.getName() : null,
            filter != null ? filter.getDescription() : null,
            filter != null ? filter.getBrand() : null,
            filter != null ? filter.getCategory() : null);

    SliceResult<ProductSkuResponseDTO> response = productSkuDsGateway.list(request, cursor, size);

    return productSkuPresenter.prepareListSuccessView(response);
  }
}
