package com.ndlcommerce.useCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.config.EmailService;
import com.ndlcommerce.entity.enums.UserType;
import com.ndlcommerce.entity.factory.interfaces.UserFactory;
import com.ndlcommerce.entity.model.implementation.CommonUser;
import com.ndlcommerce.entity.model.interfaces.User;
import com.ndlcommerce.useCase.interfaces.emailValidation.EmailValidationDsGateway;
import com.ndlcommerce.useCase.interfaces.user.UserPresenter;
import com.ndlcommerce.useCase.interfaces.user.UserRegisterDsGateway;
import com.ndlcommerce.useCase.request.user.UserDbRequestDTO;
import com.ndlcommerce.useCase.request.user.UserRequestDTO;
import com.ndlcommerce.useCase.request.user.UserResponseDTO;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class UserRegisterInteractorTests {

  private UserRegisterDsGateway userDsGateway;
  private UserPresenter userPresenter;
  private UserFactory userFactory;
  private UserRegisterInteractor interactor;
  private EmailValidationDsGateway emailValidationDsGateway;
  private EmailService emailService;

  @BeforeEach
  void setUp() {
    userDsGateway = mock(UserRegisterDsGateway.class);
    userPresenter = mock(UserPresenter.class);
    userFactory = mock(UserFactory.class);
    emailValidationDsGateway = mock(EmailValidationDsGateway.class);
    emailService = mock(EmailService.class);
    interactor =
        new UserRegisterInteractor(
            userDsGateway, userPresenter, userFactory, emailValidationDsGateway, emailService);
  }

  @Test
  void givenBaeldungUserAndAa123456Password_whenCreate_thenSaveItAndPrepareSuccessView() {
    User user = new CommonUser("baeldung", "baeldung", UserType.COMMON, "Senha1234");
    UserRequestDTO userRequestDTO =
        new UserRequestDTO(user.getLogin(), user.getType(), user.getEmail(), user.getPassword());

    UserDataMapper fakeSavedUser =
        new UserDataMapper(
            user.getLogin(),
            user.getEmail(),
            user.getType(),
            user.getPassword(),
            UUID.randomUUID());

    fakeSavedUser.setCreationTime(LocalDateTime.now());

    when(userFactory.create(anyString(), anyString(), any(), anyString()))
        .thenReturn(
            new CommonUser(user.getLogin(), user.getEmail(), user.getType(), user.getPassword()));
    when(userDsGateway.existsByLogin(anyString())).thenReturn(false);

    when(userDsGateway.save(any(UserDbRequestDTO.class))).thenReturn(fakeSavedUser);

    interactor.create(userRequestDTO);

    verify(userDsGateway, times(1)).save(any(UserDbRequestDTO.class));
    verify(userPresenter, times(1)).prepareSuccessView(any(UserResponseDTO.class));
  }

  @Test
  void givenNullFilter_whenListUsers_thenUseEmptyFilterAndReturnListSuccess() {
    ArgumentCaptor<UserDbRequestDTO> filterCaptor = ArgumentCaptor.forClass(UserDbRequestDTO.class);
    List<UserResponseDTO> emptyResponse = List.of();

    when(userDsGateway.list(any(UserDbRequestDTO.class))).thenReturn(List.of());
    when(userPresenter.prepareListSuccessView(any())).thenReturn(emptyResponse);

    List<UserResponseDTO> response = interactor.list(null, 0, 10);

    assertThat(response).isSameAs(emptyResponse);
    verify(userDsGateway).list(filterCaptor.capture());
    UserDbRequestDTO capturedFilter = filterCaptor.getValue();
    assertThat(capturedFilter.getLogin()).isNull();
    assertThat(capturedFilter.getEmail()).isNull();
    assertThat(capturedFilter.getType()).isNull();
    verify(userPresenter).prepareListSuccessView(any());
  }

  @Test
  void givenExistingEmail_whenCreate_thenReturnFailView() {
    User user = new CommonUser("baeldung", "baeldung", UserType.COMMON, "Senha1234");

    UserRequestDTO userRequestDTO =
        new UserRequestDTO(user.getLogin(), user.getType(), user.getEmail(), user.getPassword());

    UserDataMapper fakeSavedUser =
        new UserDataMapper(
            user.getLogin(),
            user.getEmail(),
            user.getType(),
            user.getPassword(),
            UUID.randomUUID());

    when(userDsGateway.existsByLogin(anyString())).thenReturn(false);
    when(userDsGateway.existsByEmail(anyString())).thenReturn(true);

    interactor.create(userRequestDTO);

    verify(userPresenter, times(1)).prepareFailView(anyString());
    verify(userDsGateway, times(0)).save(any(UserDbRequestDTO.class));
    verify(userPresenter, times(0)).prepareSuccessView(any(UserResponseDTO.class));
  }
}
