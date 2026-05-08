package com.ndlcommerce.adapters.web;

import com.ndlcommerce.useCase.interfaces.category.CategoryInputBoundary;
import com.ndlcommerce.useCase.request.category.CategoryRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.*;

@RestController
@EnableMethodSecurity
@RequestMapping("/category")
@Tag(name = "Category")
public class CategoryController {

  private final CategoryInputBoundary categoryInputBoundary;

  public CategoryController(CategoryInputBoundary categoryInputBoundary) {
    this.categoryInputBoundary = categoryInputBoundary;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  @Operation(summary = "Criar", description = "End-point para cadastrar Categorias")
  @ApiResponses({
    @ApiResponse(responseCode = "201", description = "Cadastrado com sucesso Categorias")
  })
  public ResponseEntity<?> create(@Valid @RequestBody CategoryRequestDTO requestModel) {
    var productCreated = categoryInputBoundary.create(requestModel);
    return ResponseEntity.ok().body(productCreated);
  }

  @PostMapping("/search")
  @Operation(summary = "Listar", description = "End-point para listar todas as Categorias")
  public ResponseEntity<?> listCategories(
      @RequestBody(required = false) CategoryRequestDTO filter,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    CategoryRequestDTO categoryFilter = filter != null ? filter : new CategoryRequestDTO();
    var result = categoryInputBoundary.list(categoryFilter, page, size);
    return ResponseEntity.ok().body(result);
  }

  @GetMapping("{id}")
  public ResponseEntity<?> getById(@PathVariable("id") String categoryId) {
    var result = categoryInputBoundary.getById(UUID.fromString(categoryId));
    return ResponseEntity.ok().body(result);
  }

  @PutMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> update(
      @PathVariable("id") String categoryId, @RequestBody CategoryRequestDTO requestModel) {
    var result = categoryInputBoundary.updateCategory(UUID.fromString(categoryId), requestModel);
    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> delete(@PathVariable("id") String categoryId) {
    var result = categoryInputBoundary.deleteCategory(UUID.fromString(categoryId));
    return ResponseEntity.ok().body(result);
  }
}
