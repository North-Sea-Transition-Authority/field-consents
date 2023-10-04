package uk.co.nstauthority.fieldconsents.energyportal.user;

import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.util.userutil.UserDisplayNameUtil;

public record EnergyPortalUserDto(
    Long webUserAccountId,
    Long personId,
    String title,
    String forename,
    String surname,
    String emailAddress,
    String telephoneNumber,
    boolean isSharedAccount,
    boolean canLogin
) {

  public static EnergyPortalUserDto from(User user) {
    return new EnergyPortalUserDto(
        user.getWebUserAccountId().longValue(),
        user.getPersonId().longValue(),
        user.getTitle(),
        user.getForename(),
        user.getSurname(),
        user.getPrimaryEmailAddress(),
        user.getTelephoneNumber(),
        user.getIsAccountShared(),
        user.getCanLogin()
    );
  }

  public String displayName() {
    return UserDisplayNameUtil.getUserDisplayName(forename, surname);
  }
}
