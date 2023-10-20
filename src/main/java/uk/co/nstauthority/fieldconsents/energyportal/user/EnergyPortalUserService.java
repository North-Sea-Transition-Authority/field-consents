package uk.co.nstauthority.fieldconsents.energyportal.user;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.fivium.energyportalapi.generated.client.UserProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.UsersProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.api.EnergyPortalApiWrapper;

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

  private final EnergyPortalApiWrapper energyPortalApiWrapper;

  @Autowired
  public EnergyPortalUserService(UserApi userApi, EnergyPortalApiWrapper energyPortalApiWrapper) {
    this.userApi = userApi;
    this.energyPortalApiWrapper = energyPortalApiWrapper;
  }

  public List<EnergyPortalUserDto> findUserByUsername(String username) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) ->
        userApi.searchUsersByEmail(
            username,
            USERS_PROJECT_ROOT,
            new RequestPurpose(requestPurpose.purpose()),
            new LogCorrelationId(logCorrelationId.id())
        )
        .stream()
        .filter(User::getCanLogin)
        .map(EnergyPortalUserDto::from)
        .toList()
    ));
  }

  public List<EnergyPortalUserDto> findByWuaIds(Collection<WebUserAccountId> webUserAccountIds) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> {

      List<Integer> webUserAccountIdApiInputs = webUserAccountIds
          .stream()
          .map(WebUserAccountId::toInt)
          .toList();

      return userApi.searchUsersByIds(
              webUserAccountIdApiInputs,
              USERS_PROJECT_ROOT,
              new RequestPurpose(requestPurpose.purpose()),
              new LogCorrelationId(logCorrelationId.id())
          )
          .stream()
          .map(EnergyPortalUserDto::from)
          .toList();
    }));
  }

  public Map<WebUserAccountId, EnergyPortalUserDto> getEnergyPortalUserMap(List<WebUserAccountId> webUserAccountIds) {
    return findByWuaIds(webUserAccountIds)
        .stream()
        .collect(Collectors.toMap(
            energyPortalUser -> WebUserAccountId.from(energyPortalUser.webUserAccountId()),
            Function.identity()
        ));
  }

  public Optional<EnergyPortalUserDto> findByWuaId(WebUserAccountId webUserAccountId) {
    return energyPortalApiWrapper.makeRequest(((logCorrelationId, requestPurpose) -> userApi.findUserById(
            webUserAccountId.toInt(),
            USER_PROJECT_ROOT,
            new RequestPurpose(requestPurpose.purpose()),
            new LogCorrelationId(logCorrelationId.id())
        )
        .stream()
        .map(EnergyPortalUserDto::from)
        .findFirst()
    ));
  }

  public EnergyPortalUserDto getByWuaId(WebUserAccountId webUserAccountId) {
    return findByWuaId(webUserAccountId)
        .orElseThrow(() ->
            new EntityNotFoundException("Energy portal user with wua id %s not found"
                .formatted(webUserAccountId.toString())));
  }
}
