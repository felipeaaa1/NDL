package com.ndlcommerce.adapters.persistence.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.config.SecurityFilter;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class JpaBrandTest {

  @Test
  void givenFilterAndPagination_whenList_thenMapResultAndUseDeterministicSort() {
    JpaBrandRepository repository = mock(JpaBrandRepository.class);
    JpaBrand adapter = new JpaBrand(repository, mock(SecurityFilter.class));
    BrandDataMapper mapper = new BrandDataMapper("Nike", UUID.randomUUID());
    mapper.setId(UUID.randomUUID());
    mapper.setCreatedAt(LocalDateTime.now());
    when(repository.findAll(any(), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(mapper)));
    ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

    PaginatedResult<BrandGatewayResponseDTO> result =
        adapter.list(new BrandDbRequestDTO("Nik"), 0, 15);

    verify(repository).findAll(any(), pageableCaptor.capture());
    Pageable pageable = pageableCaptor.getValue();
    assertThat(pageable.getPageNumber()).isZero();
    assertThat(pageable.getPageSize()).isEqualTo(15);
    assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.ASC);
    assertThat(pageable.getSort().getOrderFor("id").getDirection()).isEqualTo(Sort.Direction.ASC);
    assertThat(result.getContent())
        .containsExactly(
            new BrandGatewayResponseDTO(mapper.getId(), "Nike", mapper.getCreatedAt()));
  }
}
