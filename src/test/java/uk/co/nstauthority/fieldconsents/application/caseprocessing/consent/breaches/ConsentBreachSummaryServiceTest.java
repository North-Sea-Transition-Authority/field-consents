package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@ExtendWith(MockitoExtension.class)
class ConsentBreachSummaryServiceTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  private ConsentBreachSummaryService consentBreachSummaryService;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    consentBreachSummaryService = new ConsentBreachSummaryService(energyPortalUserService);

    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getConsentBreachView() {

    var consentBreachId = 13;
    var breachText = "Test Breach Text";
    var instant = Instant.now();
    var dateTimeAsString = DateUtils.format(instant, DATE_TIME);
    var consentId = 3;
    var consent = new Consent(consentId);

    var consentBreach = new ConsentBreach(consentBreachId);
    consentBreach.setConsent(consent);
    consentBreach.setBreachText(breachText);
    consentBreach.setAddedByWuaId(user.wuaId());
    consentBreach.setAddedDateTime(instant);

    var forename = "test";
    var surname = "user";
    var fullname = forename + " " + surname;

    var energyPortalUserDTO = EnergyPortalUserDtoTestUtil.Builder()
        .withForename("test")
        .withSurname("user")
        .build();

    when(energyPortalUserService.getByWuaId(new WebUserAccountId(user.wuaId())))
        .thenReturn(energyPortalUserDTO);

    assertThat(consentBreachSummaryService.getConsentBreachView(consentBreach))
        .extracting(ConsentBreachView::breachText,
            ConsentBreachView::addedByUser,
            ConsentBreachView::addedDateTime).containsExactly(
            breachText,
            fullname,
            dateTimeAsString);
  }
}
