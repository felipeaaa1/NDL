package com.ndlcommerce.useCase;

import com.ndlcommerce.adapters.persistence.emailValidaton.EmailValidationDataMapper;
import com.ndlcommerce.adapters.persistence.user.UserDataMapper;
import com.ndlcommerce.config.EmailService;
import com.ndlcommerce.entity.factory.interfaces.UserFactory;
import com.ndlcommerce.entity.model.implementation.CommonUser;
import com.ndlcommerce.entity.model.interfaces.User;
import com.ndlcommerce.useCase.interfaces.emailValidation.EmailValidationDsGateway;
import com.ndlcommerce.useCase.interfaces.user.UserInputBoundary;
import com.ndlcommerce.useCase.interfaces.user.UserPresenter;
import com.ndlcommerce.useCase.interfaces.user.UserRegisterDsGateway;
import com.ndlcommerce.useCase.request.user.*;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class UserRegisterInteractor implements UserInputBoundary {

  final UserRegisterDsGateway userDsGateway;
  final UserPresenter userPresenter;
  final UserFactory userFactory;
  final EmailValidationDsGateway emailValidationDsGateway;
  final EmailService emailService;

  public UserRegisterInteractor(
      UserRegisterDsGateway userDsGateway,
      UserPresenter userPresenter,
      UserFactory userFactory,
      EmailValidationDsGateway emailValidationDsGateway,
      EmailService emailService) {
    this.userDsGateway = userDsGateway;
    this.userPresenter = userPresenter;
    this.userFactory = userFactory;
    this.emailValidationDsGateway = emailValidationDsGateway;
    this.emailService = emailService;
  }

  @Override
  public UserResponseDTO create(UserRequestDTO requestModel) {
    if (userDsGateway.existsByLogin(requestModel.getLogin())) {
      return userPresenter.prepareFailView("existsByName");
    }
    if (userDsGateway.existsByEmail(requestModel.getEmail())) {
      return userPresenter.prepareFailView("existsByEmail");
    }
    User user =
        userFactory.create(
            requestModel.getLogin(),
            requestModel.getEmail(),
            requestModel.getType(),
            requestModel.getPassword());
    if (!user.passwordIsValid()) {
      return userPresenter.prepareFailView("passwordIsValid");
    }
    if (!user.loginIsValid()) {
      return userPresenter.prepareFailView("nameIsValid");
    }
    UserDbRequestDTO userDsModel =
        new UserDbRequestDTO(user.getLogin(), user.getEmail(), user.getType(), user.getPassword());

    UserDataMapper save = userDsGateway.save(userDsModel);

    UserResponseDTO accountResponseModel =
        new UserResponseDTO(
            save.getId(),
            save.getLogin(),
            save.getEmail(),
            save.getType().toString(),
            save.getCreationTime().toString());
    return userPresenter.prepareSuccessView(accountResponseModel);
  }

  @Override
  public List<UserResponseDTO> list(UserFilterDTO userFilterDTO, int page, int size) {

    UserDbRequestDTO userDbRequestDTO =
        new UserDbRequestDTO(
            userFilterDTO != null ? userFilterDTO.getLogin() : null,
            userFilterDTO != null ? userFilterDTO.getEmail() : null,
            userFilterDTO != null ? userFilterDTO.getType() : null);

    List<UserDataMapper> list = userDsGateway.list(userDbRequestDTO);
    List<UserResponseDTO> responseList =
        list.stream()
            .map(
                user ->
                    new UserResponseDTO(
                        user.getId(),
                        user.getLogin(),
                        user.getEmail(),
                        user.getType().toString(),
                        user.getCreationTime().toString()))
            .toList();

    return userPresenter.prepareListSuccessView(responseList);
  }

  @Override
  public UserResponseDTO getById(UUID userId) {

    Optional<UserDataMapper> opt = userDsGateway.getById(userId);

    if (opt.isEmpty()) {
      return userPresenter.prepareFailView("NotFound");
    }
    UserDataMapper user = opt.get();
    UserResponseDTO accountResponseModel =
        new UserResponseDTO(
            user.getId(),
            user.getLogin(),
            user.getEmail(),
            user.getType().toString(),
            user.getCreationTime().toString());
    return userPresenter.prepareSuccessView(accountResponseModel);
  }

  @Override
  public UserResponseDTO updateUser(UUID userId, UpdateUserDTO requestModel) {
    Optional<UserDataMapper> opt = userDsGateway.getById(userId);

    if (opt.isEmpty()) {
      return userPresenter.prepareFailView("NotFound");
    }

    if (requestModel.getLogin() != null
        && userDsGateway.existsByLoginAndNotId(requestModel.getLogin(), userId)) {
      return userPresenter.prepareFailView("existsByName");
    }
    if (requestModel.getEmail() != null
        && userDsGateway.existsByEmailAndNotId(requestModel.getEmail(), userId)) {
      return userPresenter.prepareFailView("existsByEmail");
    }

    UserDataMapper userDB = opt.get();
    User user =
        new CommonUser(requestModel.getLogin(), requestModel.getEmail(), requestModel.getType());

    if (requestModel.getLogin() != null && !user.loginIsValid()) {
      return userPresenter.prepareFailView("nameIsValid");
    }

    UserDbRequestDTO userDsModel =
        new UserDbRequestDTO(user.getLogin(), user.getEmail(), user.getType());

    UserDataMapper update = userDsGateway.update(userId, userDsModel);

    UserResponseDTO accountResponseModel =
        new UserResponseDTO(
            update.getId(),
            update.getLogin(),
            update.getEmail(),
            update.getType().toString(),
            update.getCreationTime().toString());

    return userPresenter.prepareSuccessView(accountResponseModel);
  }

  @Override
  public UserResponseDTO deleteUser(UUID uuid) {

    //    TODO: validar deletar user colocando acrtive como false;
    Optional<UserDataMapper> opt = userDsGateway.getById(uuid);

    if (opt.isEmpty()) {
      return userPresenter.prepareFailView("NotFound");
    }

    UserDbRequestDTO userDsModel =
        new UserDbRequestDTO(opt.get().getLogin(), opt.get().getEmail(), opt.get().getType());
    userDsModel.setActive(false);
    UserDataMapper update = userDsGateway.update(uuid, userDsModel);

    return userPresenter.prepareSuccessView(null);
  }

  @Override
  public UserResponseDTO sendValidationToken(UUID userId) throws IOException {
    Optional<UserDataMapper> opt = userDsGateway.getById(userId);
    if (opt.isEmpty()) {
      return userPresenter.prepareFailView("NotFound");
    }

    emailValidationDsGateway.markUsedPrevipousTokens(opt.get());
    UUID validationToken = emailValidationDsGateway.createValidationToken(opt.get());

    emailService.sendValidationEmail(opt.get().getEmail(), "Validação de email", validationToken);

    return userPresenter.prepareSuccessView(null);
  }

  @Override
  public UserResponseDTO verifyEmail(UUID uuid) {

    Optional<EmailValidationDataMapper> byId = emailValidationDsGateway.getById(uuid);

    if (byId.isEmpty()) {
      return userPresenter.prepareFailView("NotFound");
    }
    EmailValidationDataMapper emailValidationDataMapper = byId.get();

    if (emailValidationDataMapper.isExpired() || emailValidationDataMapper.isUsed()) {
      return userPresenter.prepareFailView("TokenNotValid");
    }

    Optional<UserDataMapper> tokensUser =
        userDsGateway.getById(emailValidationDataMapper.getUser().getId());
    if (tokensUser.isEmpty()) {
      return userPresenter.prepareFailView("UserRemoved");
    }

    userDsGateway.validateEmail(tokensUser.get().getId());
    emailValidationDsGateway.markUsedToken(emailValidationDataMapper);

    return userPresenter.prepareSuccessView(null);
  }
}
