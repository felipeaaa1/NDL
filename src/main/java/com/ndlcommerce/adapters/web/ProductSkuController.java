package com.ndlcommerce.adapters.web;

import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuInputBoundary;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableMethodSecurity
@RequestMapping("/product/sku")
@Tag(name = "Produto")
public class ProductSkuController {

  private final ProductSkuInputBoundary productSkuInputBoundary;

  public ProductSkuController(ProductSkuInputBoundary productSkuInputBoundary) {
    this.productSkuInputBoundary = productSkuInputBoundary;
  }

  @GetMapping
  public ResponseEntity<?> listProductsSku(
      @Nullable ProductFilterDTO filter,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "15") @Max(50) @Min(1) int size) {
    var result = productSkuInputBoundary.list(filter, page, size);
    return ResponseEntity.ok().body(result);
  }
}
