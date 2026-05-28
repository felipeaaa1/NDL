package com.ndlcommerce.adapters.web;

import com.ndlcommerce.useCase.interfaces.product.ProductInputBoundary;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import com.ndlcommerce.useCase.request.product.ProductRequestDTO;
import com.ndlcommerce.useCase.request.product.ProductUpdateRequestDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableMethodSecurity
@RequestMapping({"/product"})
@Tag(name = "Produto")
public class ProductController {

  private final ProductInputBoundary productInputBoundary;

  public ProductController(ProductInputBoundary productInputBoundary) {
    this.productInputBoundary = productInputBoundary;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<?> create(@Valid @RequestBody ProductRequestDTO requestModel) {
    var productCreated = productInputBoundary.create(requestModel);
    return ResponseEntity.ok().body(productCreated);
  }

  @GetMapping
  public ResponseEntity<?> listProducts(
      @Nullable ProductFilterDTO filter,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "15")
      @Max(50)
      @Min(1)
      int size) {
    var result = productInputBoundary.list(filter, page, size);
    return ResponseEntity.ok().body(result);
  }

  @GetMapping("{id}")
  public ResponseEntity<?> getById(@PathVariable("id") String productId) {
    var result = productInputBoundary.getById(UUID.fromString(productId));
    return ResponseEntity.ok().body(result);
  }

  @PutMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> update(
      @PathVariable("id") String productId, @RequestBody ProductUpdateRequestDTO requestModel) {
    var result = productInputBoundary.updateProduct(UUID.fromString(productId), requestModel);
    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> delete(@PathVariable("id") String productId) {
    var result = productInputBoundary.deleteProduct(UUID.fromString(productId));
    return ResponseEntity.ok().body(result);
  }
}
