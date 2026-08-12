package com.ndlcommerce.adapters.web;

import com.ndlcommerce.useCase.interfaces.brand.BrandInputBoundary;
import com.ndlcommerce.useCase.request.brand.BrandFilterDTO;
import com.ndlcommerce.useCase.request.brand.BrandRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableMethodSecurity
@RequestMapping("/brand")
@Tag(name = "Brand")
public class BrandController {

  private final BrandInputBoundary brandInputBoundary;

  public BrandController(BrandInputBoundary brandInputBoundary) {
    this.brandInputBoundary = brandInputBoundary;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Criar", description = "End-point para cadastrar Marcas")
  @ApiResponses({@ApiResponse(responseCode = "201", description = "Cadastrado com sucesso Marcas")})
  public ResponseEntity<?> create(@Valid @RequestBody BrandRequestDTO requestModel) {
    var brandCreated = brandInputBoundary.create(requestModel);
    return ResponseEntity.ok().body(brandCreated);
  }

  @GetMapping
  @Operation(summary = "Listar", description = "End-point para listar todas as Marcas")
  public ResponseEntity<?> listBrands(
      @RequestParam(name = "name", required = false) String name,
      @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(name = "size", defaultValue = "15") @Min(1) @Max(50) int size) {
    var result = brandInputBoundary.list(new BrandFilterDTO(name), page, size);
    return ResponseEntity.ok().body(result);
  }

  @PostMapping("/search")
  @Operation(
      summary = "Listar (legado)",
      description = "End-point legado; prefira GET /brand com o parâmetro name")
  public ResponseEntity<?> searchBrands(
      @RequestBody(required = false) BrandRequestDTO filter,
      @RequestParam(name = "page", defaultValue = "0") @Min(0) int page,
      @RequestParam(name = "size", defaultValue = "15") @Min(1) @Max(50) int size) {
    var result =
        brandInputBoundary.list(
            new BrandFilterDTO(filter != null ? filter.name() : null), page, size);
    return ResponseEntity.ok().body(result);
  }

  @GetMapping("{id}")
  public ResponseEntity<?> getById(@PathVariable("id") String brandId) {
    var result = brandInputBoundary.getById(UUID.fromString(brandId));
    return ResponseEntity.ok().body(result);
  }

  @PutMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> update(
      @PathVariable("id") String brandId, @RequestBody BrandRequestDTO requestModel) {
    var result = brandInputBoundary.updateBrand(UUID.fromString(brandId), requestModel);
    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> delete(@PathVariable("id") String brandId) {
    var result = brandInputBoundary.deleteBrand(UUID.fromString(brandId));
    return ResponseEntity.ok().body(result);
  }
}
