package com.ndlcommerce.domain.factory.implementation;

import com.ndlcommerce.domain.factory.interfaces.CustomerFactory;
import com.ndlcommerce.domain.model.implementation.CommonCustomer;
import com.ndlcommerce.domain.model.interfaces.Customer;
import java.util.UUID;

public class CommonCustomerFactoryImp implements CustomerFactory {

  @Override
  public Customer create(UUID userId, String name, String contact, String address) {
    return new CommonCustomer(userId, name, contact, address);
  }
}
