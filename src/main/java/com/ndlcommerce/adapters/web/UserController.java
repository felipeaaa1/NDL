package com.ndlcommerce.adapters.web;

import com.ndlcommerce.useCase.interfaces.user.UserInputBoundary;
import com.ndlcommerce.useCase.request.user.UpdateUserDTO;
import com.ndlcommerce.useCase.request.user.UserFilterDTO;
import com.ndlcommerce.useCase.request.user.UserRequestDTO;
import com.ndlcommerce.useCase.request.user.UserResponseDTO;
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
@RequestMapping("/users")
@Tag(name = "Usuário")
public class UserController {

  private final UserInputBoundary userInput;

  public UserController(UserInputBoundary userInput) {
    this.userInput = userInput;
  }

  @PostMapping
  @PreAuthorize("hasAnyRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseEntity<?> create(@Valid @RequestBody UserRequestDTO requestModel) {
    var userCreated = userInput.create(requestModel);
    return ResponseEntity.ok().body(userCreated);
  }

  @PostMapping("/search")
  public ResponseEntity<?> listUsers(
      @RequestBody(required = false) UserFilterDTO filter,
      @RequestParam(name = "page", defaultValue = "0") int page,
      @RequestParam(name = "size", defaultValue = "10") int size) {
    UserFilterDTO userFilter = filter != null ? filter : new UserFilterDTO();
    var result = userInput.list(userFilter, page, size);
    return ResponseEntity.ok().body(result);
  }

  @GetMapping("{id}")
  public ResponseEntity<?> getUserById(@PathVariable("id") String id) {
    var result = userInput.getById(UUID.fromString(id));
    return ResponseEntity.ok().body(result);
  }

  @PutMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> updateUser(
      @PathVariable("id") String id, @RequestBody UpdateUserDTO requestModel) {
    var result = userInput.updateUser(UUID.fromString(id), requestModel);
    return ResponseEntity.ok().body(result);
  }

  @DeleteMapping("{id}")
  @PreAuthorize("hasAnyRole('ADMIN')")
  public ResponseEntity<?> deleteUser(@PathVariable("id") String id) {
    UserResponseDTO userResponseDTO = userInput.deleteUser(UUID.fromString(id));
    return ResponseEntity.ok().body(userResponseDTO);
  }
}
