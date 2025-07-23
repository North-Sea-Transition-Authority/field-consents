package uk.co.nstauthority.fieldconsents.authentication;


import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.util.userutil.UserDisplayNameUtil;

class ServiceUserDetailTest {

  @Test
  void from() {
    var energyPortalUser = EnergyPortalUserDtoTestUtil.Builder().build();
    assertThat(ServiceUserDetail.from(energyPortalUser))
        .isEqualTo(
            new ServiceUserDetail(
                energyPortalUser.webUserAccountId(),
                energyPortalUser.personId(),
                energyPortalUser.forename(),
                energyPortalUser.surname(),
                energyPortalUser.emailAddress(),
                null,
                null
            )
        );
  }

  @Test
  void getName_displayName_displayNameAndEmail_displayNameIncludingAnyProxyUser_whenNoProxyUserExists() {
    var serviceUserDetail = ServiceUserDetailTestUtil.Builder()
        .buildWithoutProxy();
    assertThat(serviceUserDetail.getName())
        .isEqualTo(serviceUserDetail.wuaId().toString());
    assertThat(serviceUserDetail.displayName())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayName(serviceUserDetail.forename(), serviceUserDetail.surname()));
    assertThat(serviceUserDetail.displayNameAndEmail())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayNameAndEmail(
            serviceUserDetail.forename(), serviceUserDetail.surname(), serviceUserDetail.emailAddress()));
    assertThat(serviceUserDetail.displayNameIncludingAnyProxyUser())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayName(serviceUserDetail.forename(), serviceUserDetail.surname()));
  }

  @Test
  void getName_displayName_displayNameAndEmail_displayNameIncludingAnyProxyUser_whenProxyUserExists() {
    var serviceUserDetail = ServiceUserDetailTestUtil.Builder().build();
    assertThat(serviceUserDetail.getName())
        .isEqualTo(serviceUserDetail.proxyWuaId().toString());
    assertThat(serviceUserDetail.displayName())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayName(serviceUserDetail.forename(), serviceUserDetail.surname()));
    assertThat(serviceUserDetail.displayNameAndEmail())
        .isEqualTo(UserDisplayNameUtil.getUserDisplayNameAndEmail(
            serviceUserDetail.forename(), serviceUserDetail.surname(), serviceUserDetail.emailAddress()));
    assertThat(serviceUserDetail.displayNameIncludingAnyProxyUser())
        .isEqualTo(String.format("%s as %s",
            serviceUserDetail.proxyUsername(),
            UserDisplayNameUtil.getUserDisplayName(serviceUserDetail.forename(), serviceUserDetail.surname())
        ));
  }
}
