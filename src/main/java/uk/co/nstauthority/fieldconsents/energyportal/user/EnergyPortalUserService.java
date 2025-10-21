package uk.co.nstauthority.fieldconsents.energyportal.user;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

@Service
public class EnergyPortalUserService {

  static final UsersProjectionRoot USERS_PROJECT_ROOT = new UsersProjectionRoot()
      .webUserAccountId()
      .personId()
      .title()
      .forename()
      .surname()
      .primaryEmailAddress()
      .telephoneNumber()
      .canLogin()
      .isAccountShared();

  static final UserProjectionRoot USER_PROJECT_ROOT = new UserProjectionRoot()
      .webUserAccountId()
      .personId()
      .title()
      .forename()
      .surname()
      .primaryEmailAddress()
      .telephoneNumber()
      .canLogin()
      .isAccountShared();

  private final UserApi userApi;

  EnergyPortalUserService(UserApi userApi) {
    this.userApi = userApi;
  }

  public Optional<User> getEnergyPortalUsersThatCanLogin(String emailAddress) {
    var users = userApi.searchUsersByEmail(
            emailAddress,
            USERS_PROJECT_ROOT,
            new RequestPurpose("findUserByUsername")
        )
        .stream()
        .filter(User::getCanLogin)
        .toList();
    if (users.size() > 1) {
      throw new IllegalStateException(
          "More than one UK Energy Portal user exists with the email address %s".formatted(emailAddress)
      );
    }
    return users
        .stream()
        .findFirst();
  }

  public List<EnergyPortalUserDto> findByWuaIds(Collection<WebUserAccountId> webUserAccountIds) {
    if (webUserAccountIds.isEmpty()) {
      return List.of();
    }

    List<Long> webUserAccountIdApiInputs = webUserAccountIds
        .stream()
        .map(WebUserAccountId::id)
        .toList();

    return userApi.searchUsersByIds(
            webUserAccountIdApiInputs,
            USERS_PROJECT_ROOT,
            new RequestPurpose("findByWuaIds")
        )
        .stream()
        .map(EnergyPortalUserDto::from)
        .sorted(Comparator.comparing(EnergyPortalUserDto::displayName))
        .toList();
  }

  public Map<WebUserAccountId, EnergyPortalUserDto> getEnergyPortalUserMap(
      Collection<WebUserAccountId> webUserAccountIds
  ) {
    return findByWuaIds(webUserAccountIds)
        .stream()
        .collect(Collectors.toMap(
            energyPortalUser -> WebUserAccountId.from(energyPortalUser.webUserAccountId()),
            Function.identity()
        ));
  }

  public Optional<EnergyPortalUserDto> findByWuaId(WebUserAccountId webUserAccountId) {
    return userApi.findUserById(
            webUserAccountId.toInt(),
            USER_PROJECT_ROOT,
            new RequestPurpose("findByWuaId")
        )
        .stream()
        .map(EnergyPortalUserDto::from)
        .findFirst();
  }

  public EnergyPortalUserDto getByWuaId(WebUserAccountId webUserAccountId) {
    return findByWuaId(webUserAccountId)
        .orElseThrow(() ->
            new EntityNotFoundException("Energy portal user with wua id %s not found"
                .formatted(webUserAccountId.toString())));
  }

  public ServiceUserDetail getServiceUserByWuaId(WebUserAccountId webUserAccountId) {
    return ServiceUserDetail.from(getByWuaId(webUserAccountId));
  }
}
