package com.ndlcommerce.domain.factory.interfaces;

import com.ndlcommerce.domain.model.interfaces.Customer;
import java.util.UUID;

public interface CustomerFactory {
  Customer create(UUID userId, String name, String contact, String address);
}
