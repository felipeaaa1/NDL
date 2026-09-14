package com.ndlcommerce.adapters.web;

import com.ndlcommerce.adapters.web.cursor.CursorUtil;
import com.ndlcommerce.useCase.interfaces.productSku.ProductSkuInputBoundary;
import com.ndlcommerce.useCase.model.OrderCursor;
import com.ndlcommerce.useCase.request.product.ProductFilterDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
      @RequestParam(name = "cursor", required = false) String cursor,
      @RequestParam(name = "size", defaultValue = "15") @Max(50) @Min(1) int size) {

    OrderCursor orderCursor = cursor == null || cursor.isBlank() ? null : CursorUtil.decode(cursor);
    Map<String, Object> listProductSkuResponseDTO =
        productSkuInputBoundary.list(filter, orderCursor, size);

    return ResponseEntity.ok().body(listProductSkuResponseDTO);
  }
}
