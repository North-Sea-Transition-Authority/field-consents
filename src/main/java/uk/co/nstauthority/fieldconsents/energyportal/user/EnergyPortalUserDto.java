package uk.co.nstauthority.fieldconsents.energyportal.user;

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

  public String displayName() {
    return UserDisplayNameUtil.getUserDisplayName(forename, surname);
  }
}
