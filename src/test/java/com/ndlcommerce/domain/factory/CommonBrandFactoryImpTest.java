package com.ndlcommerce.domain.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.ndlcommerce.domain.factory.implementation.CommonBrandFactoryImp;
import com.ndlcommerce.domain.factory.interfaces.BrandFactory;
import com.ndlcommerce.domain.model.implementation.CommonBrand;
import org.junit.jupiter.api.Test;

class CommonBrandFactoryImpTest {

  @Test
  void givenName_whenCreate_thenReturnsCommonBrandWithGivenName() {
    BrandFactory factory = new CommonBrandFactoryImp();

    var brand = factory.create("Nike");

    assertThat(brand).isInstanceOf(CommonBrand.class);
    assertThat(brand.getName()).isEqualTo("Nike");
  }
}
