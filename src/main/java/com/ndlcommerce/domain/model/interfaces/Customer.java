package com.ndlcommerce.domain.model.interfaces;

import java.util.UUID;

public interface Customer {
  boolean nameIsValid();

  boolean hasAddress();

  boolean isCustumerActive();

  UUID getUserId();

  String getName();

  String getContact();

  String getAddress();
}
