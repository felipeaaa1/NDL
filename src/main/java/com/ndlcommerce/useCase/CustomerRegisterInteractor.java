package com.ndlcommerce.useCase;

import com.ndlcommerce.adapters.persistence.customer.CustomerDataMapper;
import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.entity.factory.interfaces.CustomerFactory;
import com.ndlcommerce.entity.model.interfaces.Customer;
import com.ndlcommerce.useCase.interfaces.customer.CustomerInputBoundary;
import com.ndlcommerce.useCase.interfaces.customer.CustomerPresenter;
import com.ndlcommerce.useCase.interfaces.customer.CustomerRegisterDsGateway;
import com.ndlcommerce.useCase.interfaces.user.UserRegisterDsGateway;
import com.ndlcommerce.useCase.request.customer.CustomerDbRequestDTO;
import com.ndlcommerce.useCase.request.customer.CustomerFilterDTO;
import com.ndlcommerce.useCase.request.customer.CustomerRequestDTO;
import com.ndlcommerce.useCase.request.customer.CustomerResponseDTO;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerRegisterInteractor implements CustomerInputBoundary {

  final CustomerRegisterDsGateway customerRegisterDsGateway;
  final CustomerPresenter customerPresenter;
  final CustomerFactory customerFactory;
  final UserRegisterDsGateway userDsGateway;

  public CustomerRegisterInteractor(
      CustomerRegisterDsGateway customerRegisterDsGateway,
      CustomerPresenter customerPresenter,
      CustomerFactory customerFactory,
      UserRegisterDsGateway userDsGateway) {
    this.customerRegisterDsGateway = customerRegisterDsGateway;
    this.customerPresenter = customerPresenter;
    this.customerFactory = customerFactory;
    this.userDsGateway = userDsGateway;
  }

  @Override
  public CustomerResponseDTO create(CustomerRequestDTO requestModel) {
    Optional<UserDataMapper> userDataMapperOptional =
        userDsGateway.getById(requestModel.getUserID());

    if (userDataMapperOptional.isEmpty() || !userDataMapperOptional.get().isEnabled()) {
      return customerPresenter.prepareFailView("UserNotExistsOrInactive");
    }
    if (customerRegisterDsGateway.existsByName(requestModel.getName())) {
      return customerPresenter.prepareFailView("existsByName");
    }

    Customer customer =
        customerFactory.create(
            requestModel.getUserID(),
            requestModel.getName(),
            requestModel.getContact(),
            requestModel.getAddress());

    if (!customer.nameIsValid()) {
      return customerPresenter.prepareFailView("NameNotValid");
    }

    CustomerDbRequestDTO dbRequestDTO =
        new CustomerDbRequestDTO(
            customer.getName(),
            customer.getContact(),
            customer.getAddress(),
            true,
            customer.getUserId());

    CustomerDataMapper saved = customerRegisterDsGateway.save(dbRequestDTO);

    CustomerResponseDTO responseDTO =
        new CustomerResponseDTO(
            saved.getId(),
            saved.getUser().getLogin(),
            saved.getName(),
            saved.getContact(),
            saved.getCreatedAt().toString());

    return customerPresenter.prepareSuccessView(responseDTO);
  }

  @Override
  public List<CustomerResponseDTO> list(CustomerFilterDTO filter, int page, int size) {

    // DTO usado no gateway (similar ao UserDbRequestDTO)
    CustomerDbRequestDTO dbFilter =
        new CustomerDbRequestDTO(
            filter != null ? filter.getName() : null,
            filter != null ? filter.getContact() : null,
            filter != null ? filter.getAddress() : null,
            filter != null && filter.getActive() != null ? filter.getActive() : true,
            filter != null ? filter.getUserLogin() : null);

    List<CustomerDataMapper> list = customerRegisterDsGateway.list(dbFilter, page, size);

    List<CustomerResponseDTO> responseList =
        list.stream()
            .map(
                customer ->
                    new CustomerResponseDTO(
                        customer.getId(),
                        customer.getUser().getLogin(),
                        customer.getName(),
                        customer.getContact(),
                        customer.getCreatedAt().toString()))
            .toList();

    return customerPresenter.prepareListSuccessView(responseList);
  }

  @Override
  public CustomerResponseDTO getById(UUID customerId) {
    Optional<CustomerDataMapper> optional = customerRegisterDsGateway.findById(customerId);
    if (optional.isEmpty()) {
      return customerPresenter.prepareFailView("NotFound");
    }
    CustomerDataMapper customerDataMapper = optional.get();
    CustomerResponseDTO responseDTO =
        new CustomerResponseDTO(
            customerDataMapper.getId(),
            customerDataMapper.getUser().getLogin(),
            customerDataMapper.getName(),
            customerDataMapper.getContact(),
            customerDataMapper.getCreatedAt().toString());

    return customerPresenter.prepareSuccessView(responseDTO);
  }

  @Override
  public CustomerResponseDTO updateCustomer(UUID customerId, CustomerRequestDTO requestModel) {
    Optional<CustomerDataMapper> customerDataMapperOptional =
        customerRegisterDsGateway.findById(customerId);
    if (customerDataMapperOptional.isEmpty()) {
      return customerPresenter.prepareFailView("NotFound");
    }

    if (customerRegisterDsGateway.existsByNameAndNotID(
        customerDataMapperOptional.get().getId(), requestModel.getName())) {
      return customerPresenter.prepareFailView("existsByName");
    }

    Customer customer =
        customerFactory.create(
            requestModel.getUserID(),
            requestModel.getName(),
            requestModel.getContact(),
            requestModel.getAddress());

    if (requestModel.getName() != null && !customer.nameIsValid()) {
      return customerPresenter.prepareFailView("NameNotValid");
    }

    CustomerDbRequestDTO dbRequestDTO =
        new CustomerDbRequestDTO(
            customer.getName(),
            customer.getContact(),
            customer.getAddress(),
            true,
            customer.getUserId());

    CustomerDataMapper updated =
        customerRegisterDsGateway.update(customerDataMapperOptional.get().getId(), dbRequestDTO);

    CustomerResponseDTO responseDTO =
        new CustomerResponseDTO(
            updated.getId(),
            updated.getUser().getLogin(),
            updated.getName(),
            updated.getContact(),
            updated.getCreatedAt().toString());

    return customerPresenter.prepareSuccessView(responseDTO);
  }

  @Override
  public CustomerResponseDTO deleteCustomer(UUID uuid) {
    Optional<CustomerDataMapper> customerDataMapperOptional =
        customerRegisterDsGateway.findById(uuid);
    if (customerDataMapperOptional.isEmpty()) {
      return customerPresenter.prepareFailView("NotFound");
    }

    customerRegisterDsGateway.delete(customerDataMapperOptional.get().getId());

    return customerPresenter.prepareSuccessView(null);
  }
}
