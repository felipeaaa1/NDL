package com.ndlcommerce.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuPresenter;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuRegisterDsGateway;
import com.ndlcommerce.useCase.model.SliceResult;
import com.ndlcommerce.useCase.request.productSku.ProductSkuDbRequestDTO;
import com.ndlcommerce.useCase.request.productSku.ProductSkuResponseDTO;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class ProductSkuInteractorTest {

  @Test
  void givenNullFilter_whenList_thenUseEmptyRequestAndDelegatePagination() {
    var gateway = mock(ProductSkuRegisterDsGateway.class);
    var presenter = mock(ProductSkuPresenter.class);
    var interactor = new ProductSkuInteractor(gateway, presenter);

    SliceResult<ProductSkuResponseDTO> gatewayResult = SliceResult.of(List.of(), 0, 15, false);
    Map<String, Object> formattedResult = Map.of("data", List.of(), "nextCursor", "cursor");
    when(gateway.list(any(ProductSkuDbRequestDTO.class), eq(null), eq(15)))
        .thenReturn(gatewayResult);
    when(presenter.prepareListSuccessView(gatewayResult)).thenReturn(formattedResult);

    var result = interactor.list(null, null, 15);
    var requestCaptor = ArgumentCaptor.forClass(ProductSkuDbRequestDTO.class);

    verify(gateway).list(requestCaptor.capture(), eq(null), eq(15));
    verify(presenter).prepareListSuccessView(gatewayResult);
    assertThat(requestCaptor.getValue())
        .isEqualTo(new ProductSkuDbRequestDTO(null, null, null, null));
    assertThat(result).isSameAs(formattedResult);
  }
}
