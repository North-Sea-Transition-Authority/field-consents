package uk.co.nstauthority.fieldconsents.email;

import uk.co.fivium.digitalnotificationlibrary.core.notification.email.EmailRecipient;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;

public record FieldConsentsEmailRecipient(
    String displayName,
    String emailAddress)
    implements EmailRecipient {

  @Override
  public String getEmailAddress() {
    return emailAddress;
  }

  public static FieldConsentsEmailRecipient from(ServiceUserDetail userDetail) {
    return new FieldConsentsEmailRecipient(userDetail.displayName(), userDetail.emailAddress());
  }

  public static FieldConsentsEmailRecipient from(EnergyPortalUserDto energyPortalUserDto) {
    return new FieldConsentsEmailRecipient(energyPortalUserDto.displayName(), energyPortalUserDto.emailAddress());
  }

  public static FieldConsentsEmailRecipient from(TeamMemberView teamMemberView) {
    return new FieldConsentsEmailRecipient(teamMemberView.getDisplayName(), teamMemberView.contactEmail());
  }
}
