package com.ndlcommerce.entity.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.ndlcommerce.entity.model.implementation.CommonProduct;
import com.ndlcommerce.entity.model.implementation.CommonProductSku;
import com.ndlcommerce.entity.model.interfaces.Product;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CommonProductTest {

  @Test
  void givenShortName_whenNameIsNotValid_thenIsFalse() {

    new CommonProductSku("COR-AZ", Map.of("cor", "azul"), new BigDecimal("100.00"), 1, true);
    Product product = new CommonProduct("A", "Descrição válida");

    assertThat(product.nameIsValid()).isFalse();
  }

  @Test
  void givenNullName_whenNameIsNotValid_thenIsFalse() {

    new CommonProductSku("COR-AZ", Map.of("cor", "azul"), new BigDecimal("100.00"), 1, true);
    Product product = new CommonProduct(null, "Descrição válida");

    assertThat(product.nameIsValid()).isFalse();
  }

  @Test
  void givenHugeName_whenNameIsNotValid_thenIsFalse() {

    Product product =
        new CommonProduct(
            """
                        @Test
                        void givenNullName_whenNameIsNotValid_thenIsFalse() {
                            Product product = new CommonProduct(
                                    UUID.randomUUID(),
                                    null,
                                    "Descrição válida"
                            );

                            assertThat(product.nameIsValid()).isFalse();
                        """,
            "Descrição válida");

    assertThat(product.nameIsValid()).isFalse();
  }

  @Test
  void givenNullDescription_whenDescriptionIsNotValid_thenIsFalse() {

    Product product = new CommonProduct("Produto válido", null);

    assertThat(product.descriptionIsValid()).isFalse();
  }

  @Test
  void givenBlankDescription_whenDescriptionIsNotValid_thenIsFalse() {

    Product product = new CommonProduct("Produto válido", "   ");

    assertThat(product.descriptionIsValid()).isFalse();
  }

  @Test
  void givenHugeDescription_whenDescriptionIsNotValid_thenIsFalse() {

    Product product =
        new CommonProduct(
            "Produto válido",
            """
                        Randomize 'check' to 1-2.
                        If (check == 1) then the character is a letter.
                        Pick a random index from the letters array.
                        else
                        Pick a random number.
                        Randomize 'package com.ndlcommerce.entity.model;

                               import static org.assertj.core.api.Assertions.assertThat;

                               import com.ndlcommerce.entity.model.implementation.CommonProduct;
                               import com.ndlcommerce.entity.model.interfaces.Product;
                               import org.junit.jupiter.api.Test;

                               class CommonProductTest {

                                 @Test
                                 void givenShortName_whenNameIsNotValid_thenIsFalse() {
                                   Product product = new CommonProduct("A", "Descrição válida");

                                   assertThat(product.nameIsValid()).isFalse();
                                 }

                                 @Test
                                 void givenNullName_whenNameIsNotValid_thenIsFalse() {
                                   Product product = new CommonProduct(null, "Descrição válida");

                                   assertThat(product.nameIsValid()).isFalse();
                                 }
                                   assertThat(product.descriptionIsValid()).isFalse();
                                 }

                                 @Test
                                 void givenValidName_whenNameIsValid_thenIsTrue() {
                                   Product product = new CommonProduct("Mouse Óptico", "Perfeito para trabalho");

                                   assertThat(product.nameIsValid()).isTrue();
                                 }

                                 @Test
                                 void givenValidProduct_whenNameAndDescriptionAreValid_thenIsTrue() {
                                   Product product = new CommonProduct("Teclado Gamer", "Switch red, RGB, silencioso");

                                   assertThat(product.nameIsValid()).isTrue();
                                   assertThat(product.descriptionIsValid()).isTrue();
                                 }
                               }
                               check' to 1-2.
                        If (check == 1) then the character is a letter.
                        Pick a random index from the letters array.
                        else
                        Pick a random number.

                        """);

    assertThat(product.descriptionIsValid()).isFalse();
  }

  @Test
  void givenValidName_whenNameIsValid_thenIsTrue() {

    Product product = new CommonProduct("Mouse Óptico", "Perfeito para trabalho");

    assertThat(product.nameIsValid()).isTrue();
  }

  @Test
  void givenValidProduct_whenNameAndDescriptionAreValid_thenIsTrue() {

    Product product = new CommonProduct("Teclado Gamer", "Switch red, RGB, silencioso");

    assertThat(product.nameIsValid()).isTrue();
    assertThat(product.descriptionIsValid()).isTrue();
  }
}
