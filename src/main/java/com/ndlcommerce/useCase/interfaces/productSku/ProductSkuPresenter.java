package com.ndlcommerce.useCase.interfaces.productSku;

import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;
import java.util.Map;

public interface ProductSkuPresenter {

  Map<String, Object> prepareListSuccessView(SliceResult<ProductSkuResponseDTO> list);
}
