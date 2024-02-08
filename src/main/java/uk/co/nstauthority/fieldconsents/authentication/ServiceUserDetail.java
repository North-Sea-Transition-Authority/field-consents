package uk.co.nstauthority.fieldconsents.authentication;

import java.io.Serializable;
import org.springframework.security.core.AuthenticatedPrincipal;
import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.util.userutil.UserDisplayNameUtil;

public record ServiceUserDetail(Long wuaId,
                                Long personId,
                                String forename,
                                String surname,
                                String emailAddress)
    implements AuthenticatedPrincipal, Serializable, EmailRecipient {

  public static ServiceUserDetail from(EnergyPortalUserDto energyPortalUser) {
    return new ServiceUserDetail(
        energyPortalUser.webUserAccountId(),
        energyPortalUser.personId(),
        energyPortalUser.forename(),
        energyPortalUser.surname(),
        energyPortalUser.emailAddress()
    );
  }

  @Override
  public String getName() {
    return wuaId.toString();
  }

  public String displayName() {
    return UserDisplayNameUtil.getUserDisplayName(forename, surname);
  }

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }
}
