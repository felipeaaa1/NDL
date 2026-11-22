package com.ndlcommerce.domain.model.interfaces;

public interface Product {

  String getName();

  String getDescription();

  boolean nameIsValid();

  boolean descriptionIsValid();
}
