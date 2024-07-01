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

  public List<EnergyPortalUserDto> findUserByUsername(String username) {
    return userApi.searchUsersByEmail(
            username,
            USERS_PROJECT_ROOT,
            new RequestPurpose("findUserByUsername")
        )
        .stream()
        .filter(User::getCanLogin)
        .map(EnergyPortalUserDto::from)
        .toList();
  }

  public List<EnergyPortalUserDto> findByWuaIds(Collection<WebUserAccountId> webUserAccountIds) {
    if (webUserAccountIds.isEmpty()) {
      return List.of();
    }

    List<Integer> webUserAccountIdApiInputs = webUserAccountIds
        .stream()
        .map(WebUserAccountId::toInt)
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
}
