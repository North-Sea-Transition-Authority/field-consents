package uk.co.nstauthority.fieldconsents.authentication;

import java.io.Serializable;
import org.springframework.security.core.AuthenticatedPrincipal;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.util.userutil.UserDisplayNameUtil;

public record ServiceUserDetail(
    Long wuaId,
    Long personId,
    String forename,
    String surname,
    String emailAddress,
    Long proxyWuaId,
    String proxyUsername
) implements AuthenticatedPrincipal, Serializable {

  public static ServiceUserDetail from(EnergyPortalUserDto energyPortalUser) {
    return new ServiceUserDetail(
        energyPortalUser.webUserAccountId(),
        energyPortalUser.personId(),
        energyPortalUser.forename(),
        energyPortalUser.surname(),
        energyPortalUser.emailAddress(),
        null,
        null
    );
  }

  @Override
  public String getName() {
    // The 'name' is a unique identifier for this principal, it is not related to the users forename/surname
    return proxyWuaId != null ? proxyWuaId.toString() : wuaId.toString();
  }

  public String displayName() {
    return UserDisplayNameUtil.getUserDisplayName(forename, surname);
  }

  public String displayNameAndEmail() {
    return UserDisplayNameUtil.getUserDisplayNameAndEmail(forename, surname, emailAddress);
  }

  public String displayNameIncludingAnyProxyUser() {
    var userDisplayName = displayName();
    return proxyWuaId != null ? String.format("%s as %s", proxyUsername, userDisplayName) : userDisplayName;
  }
}
