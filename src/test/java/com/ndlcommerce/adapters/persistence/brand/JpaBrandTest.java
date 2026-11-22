package com.ndlcommerce.adapters.persistence.brand;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.config.SecurityFilter;
import com.ndlcommerce.useCase.model.PaginatedResult;
import com.ndlcommerce.useCase.request.brand.BrandDbRequestDTO;
import com.ndlcommerce.useCase.request.brand.BrandGatewayResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

class JpaBrandTest {

  private final JpaBrandRepository repository = mock(JpaBrandRepository.class);
  private final SecurityFilter securityFilter = mock(SecurityFilter.class);
  private final JpaBrand adapter = new JpaBrand(repository, securityFilter);
  private final UUID loggedUserId = UUID.randomUUID();

  private BrandDataMapper existing(UUID id, String name) {
    BrandDataMapper mapper = new BrandDataMapper(name, UUID.randomUUID());
    mapper.setId(id);
    mapper.setCreatedAt(LocalDateTime.now());
    return mapper;
  }

  private void loggedUser() {
    UserDataMapper user = new UserDataMapper();
    user.setId(loggedUserId);
    when(securityFilter.obterUsuarioLogado()).thenReturn(user);
  }

  @Test
  void givenName_whenExistsByName_thenOnlyActiveBrandsAreConsidered() {
    when(repository.existsByNameAndActiveTrue("Nike")).thenReturn(true);

    assertThat(adapter.existsByName("Nike")).isTrue();
    assertThat(adapter.existsByName("Puma")).isFalse();
  }

  @Test
  void givenNameAndId_whenExistsByNameAndNotId_thenDelegateToRepository() {
    UUID id = UUID.randomUUID();
    when(repository.existsByNameAndIdNotAndActiveTrue("Nike", id)).thenReturn(true);

    assertThat(adapter.existsByNameAndNotId("Nike", id)).isTrue();
  }

  @Test
  void givenLoggedUser_whenSave_thenSetCreatedByAndReturnGatewayResponse() {
    loggedUser();
    when(repository.save(any(BrandDataMapper.class)))
        .thenAnswer(
            invocation -> {
              BrandDataMapper saved = invocation.getArgument(0);
              saved.setId(UUID.randomUUID());
              saved.setCreatedAt(LocalDateTime.now());
              return saved;
            });
    ArgumentCaptor<BrandDataMapper> captor = ArgumentCaptor.forClass(BrandDataMapper.class);

    BrandGatewayResponseDTO result = adapter.save(new BrandDbRequestDTO("Nike"));

    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getCreatedBy()).isEqualTo(loggedUserId);
    assertThat(captor.getValue().getActive()).isTrue();
    assertThat(result.name()).isEqualTo("Nike");
    assertThat(result.id()).isNotNull();
  }

  @Test
  void givenActiveBrand_whenGetById_thenMapToGatewayResponse() {
    UUID id = UUID.randomUUID();
    BrandDataMapper mapper = existing(id, "Nike");
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(mapper));

    assertThat(adapter.getById(id))
        .contains(new BrandGatewayResponseDTO(id, "Nike", mapper.getCreatedAt()));
  }

  @Test
  void givenUnknownOrInactiveBrand_whenGetById_thenReturnEmpty() {
    UUID id = UUID.randomUUID();
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

    assertThat(adapter.getById(id)).isEmpty();
  }

  @Test
  void givenNewName_whenUpdate_thenChangeNameAndSetUpdatedBy() {
    UUID id = UUID.randomUUID();
    BrandDataMapper mapper = existing(id, "Old");
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(mapper));
    when(repository.save(mapper)).thenReturn(mapper);
    loggedUser();

    BrandGatewayResponseDTO result = adapter.update(id, new BrandDbRequestDTO("Nike"));

    assertThat(result.name()).isEqualTo("Nike");
    assertThat(mapper.getUpdatedBy()).isEqualTo(loggedUserId);
  }

  @Test
  void givenEmptyName_whenUpdate_thenKeepCurrentName() {
    UUID id = UUID.randomUUID();
    BrandDataMapper mapper = existing(id, "Old");
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(mapper));
    when(repository.save(mapper)).thenReturn(mapper);
    loggedUser();

    assertThat(adapter.update(id, new BrandDbRequestDTO("")).name()).isEqualTo("Old");
  }

  @Test
  void givenUnknownBrand_whenUpdate_thenReturnNullAndDoNotSave() {
    UUID id = UUID.randomUUID();
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

    assertThat(adapter.update(id, new BrandDbRequestDTO("Nike"))).isNull();
    verify(repository, never()).save(any());
  }

  @Test
  void givenActiveBrand_whenDelete_thenSoftDeleteAndSetUpdatedBy() {
    UUID id = UUID.randomUUID();
    BrandDataMapper mapper = existing(id, "Nike");
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.of(mapper));
    loggedUser();

    adapter.delete(id);

    verify(repository).save(mapper);
    verify(repository, never()).delete(any());
    assertThat(mapper.getActive()).isFalse();
    assertThat(mapper.getUpdatedBy()).isEqualTo(loggedUserId);
  }

  @Test
  void givenUnknownBrand_whenDelete_thenDoNothing() {
    UUID id = UUID.randomUUID();
    when(repository.findByIdAndActiveTrue(id)).thenReturn(Optional.empty());

    adapter.delete(id);

    verify(repository, never()).save(any());
  }

  @Test
  void givenNullName_whenList_thenQueryWithoutNameFilter() {
    when(repository.findAll(any(Example.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));
    ArgumentCaptor<Example<BrandDataMapper>> captor = ArgumentCaptor.forClass(Example.class);

    PaginatedResult<BrandGatewayResponseDTO> result =
        adapter.list(new BrandDbRequestDTO(null), 0, 15);

    verify(repository).findAll(captor.capture(), any(Pageable.class));
    assertThat(captor.getValue().getProbe().getName()).isNull();
    assertThat(result.getContent()).isEmpty();
  }

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
