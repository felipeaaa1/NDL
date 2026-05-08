package com.ndlcommerce.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.entity.factory.interfaces.CustomerFactory;
import com.ndlcommerce.useCase.interfaces.customer.CustomerPresenter;
import com.ndlcommerce.useCase.interfaces.customer.CustomerRegisterDsGateway;
import com.ndlcommerce.useCase.interfaces.user.UserRegisterDsGateway;
import com.ndlcommerce.useCase.request.customer.CustomerDbRequestDTO;
import com.ndlcommerce.useCase.request.customer.CustomerResponseDTO;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class CustomerRegisterInteractorTest {

  private CustomerRegisterDsGateway customerRegisterDsGateway;
  private CustomerPresenter customerPresenter;
  private CustomerFactory customerFactory;
  private UserRegisterDsGateway userDsGateway;
  private CustomerRegisterInteractor interactor;

  @BeforeEach
  void setUp() {
    customerRegisterDsGateway = mock(CustomerRegisterDsGateway.class);
    customerPresenter = mock(CustomerPresenter.class);
    customerFactory = mock(CustomerFactory.class);
    userDsGateway = mock(UserRegisterDsGateway.class);
    interactor =
        new CustomerRegisterInteractor(
            customerRegisterDsGateway, customerPresenter, customerFactory, userDsGateway);
  }

  @Test
  void givenNullFilter_whenListCustomers_thenUseEmptyFilterAndReturnListSuccess() {
    ArgumentCaptor<CustomerDbRequestDTO> filterCaptor =
        ArgumentCaptor.forClass(CustomerDbRequestDTO.class);
    List<CustomerResponseDTO> emptyResponse = List.of();

    when(customerRegisterDsGateway.list(any(CustomerDbRequestDTO.class), eq(0), eq(10)))
        .thenReturn(List.of());
    when(customerPresenter.prepareListSuccessView(any())).thenReturn(emptyResponse);

    List<CustomerResponseDTO> response = interactor.list(null, 0, 10);

    assertThat(response).isSameAs(emptyResponse);
    verify(customerRegisterDsGateway).list(filterCaptor.capture(), eq(0), eq(10));
    CustomerDbRequestDTO capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.getName()).isNull();
    assertThat(capturedFilter.getContact()).isNull();
    assertThat(capturedFilter.getAddress()).isNull();
    assertThat(capturedFilter.getUserLogin()).isNull();
    assertThat(capturedFilter.isActive()).isTrue();
    verify(customerPresenter).prepareListSuccessView(any());
  }
}
