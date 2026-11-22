package com.ndlcommerce.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.ndlcommerce.domain.model.implementation.CommonBrand;
import com.ndlcommerce.domain.model.interfaces.Brand;
import org.junit.jupiter.api.Test;

class CommonBrandTest {

  @Test
  void givenValidName_whenGetName_thenReturnsCorrectName() {
    Brand brand = new CommonBrand("Nike");

    assertThat(brand.getName()).isEqualTo("Nike");
  }

  @Test
  void givenValidName_whenNameIsValid_thenIsTrue() {
    Brand brand = new CommonBrand("Adidas");

    assertThat(brand.nameIsValid()).isTrue();
  }

  @Test
  void givenNullName_whenNameIsValid_thenIsFalse() {
    Brand brand = new CommonBrand(null);

    assertThat(brand.nameIsValid()).isFalse();
  }

  @Test
  void givenBlankName_whenNameIsValid_thenIsFalse() {
    Brand brand = new CommonBrand("   ");

    assertThat(brand.nameIsValid()).isFalse();
  }
}
