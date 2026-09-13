package com.ndlcommerce.adapters.presenter.productSku;

import com.ndlcommerce.adapters.web.cursor.CursorUtil;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuPresenter;
import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProductSkuResponseFormatter implements ProductSkuPresenter {

  @Override
  public Map<String, Object> prepareListSuccessView(
      SliceResult<ProductSkuResponseDTO> listProductSkuResponseDTO) {

    String nextCursor =
        listProductSkuResponseDTO.hasNext()
            ? CursorUtil.encode(
                new OrderCursor(
                    listProductSkuResponseDTO.items().getLast().createdAt(),
                    listProductSkuResponseDTO.items().getLast().skuId()))
            : null;

    Map<String, Object> objectMapWithCursor = new HashMap<>();
    objectMapWithCursor.put("data", listProductSkuResponseDTO.items());
    objectMapWithCursor.put("nextCursor", nextCursor);
    return objectMapWithCursor;
  }
}
