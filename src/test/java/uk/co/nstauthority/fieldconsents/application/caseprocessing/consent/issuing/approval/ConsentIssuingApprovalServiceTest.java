package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.issuing.approval;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class ConsentIssuingApprovalServiceTest {

  @Mock
  private ConsentIssuingApprovalRepository consentIssuingApprovalRepository;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private ConsentIssuingApprovalService consentIssuingApprovalService;

  private Application application;

  @BeforeEach
  void beforeEach() {
    application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    consentIssuingApprovalService =
        new ConsentIssuingApprovalService(consentIssuingApprovalRepository, energyPortalUserService, clock);
  }

  @Test
  void approveApplicationForConsentIssuing() {
    var user = ServiceUserDetailTestUtil.Builder().build();

    var consentIssuingApprovalCaptor = ArgumentCaptor.forClass(ConsentIssuingApproval.class);

    consentIssuingApprovalService.approveApplicationForConsentIssuing(application, user);

    verify(consentIssuingApprovalRepository).save(consentIssuingApprovalCaptor.capture());

    assertThat(consentIssuingApprovalCaptor.getValue())
        .isNotNull()
        .extracting(
            ConsentIssuingApproval::getApplication,
            ConsentIssuingApproval::getApprovedByWuaId,
            ConsentIssuingApproval::getApprovedInstant
        )
        .containsExactly(
            application,
            user.wuaId(),
            clock.instant()
        );
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void isApplicationApprovedForConsentIssuing(boolean consentIssuingApprovalExists) {
    when(consentIssuingApprovalRepository.existsByApplicationId(application.getId())).thenReturn(consentIssuingApprovalExists);

    assertThat(consentIssuingApprovalService.isApplicationApprovedForConsentIssuing(application))
        .isEqualTo(consentIssuingApprovalExists);
  }

  @Test
  void getConsentIssuingApprovalSummaryView_consentIssuingApprovalDoesNotExist() {
    when(consentIssuingApprovalRepository.findByApplicationId(application.getId())).thenReturn(Optional.empty());

    assertThat(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application)).isEmpty();
  }

  @Test
  void getConsentIssuingApprovalSummaryView_consentIssuingApprovalExists() {
    var consentIssuingApproval = ConsentIssuingApprovalTestUtil.newBuilder().build();

    var energyPortalUserDto = EnergyPortalUserDtoTestUtil.Builder().build();

    when(consentIssuingApprovalRepository.findByApplicationId(application.getId()))
        .thenReturn(Optional.of(consentIssuingApproval));
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(consentIssuingApproval.getApprovedByWuaId())))
        .thenReturn(energyPortalUserDto);

    var approvedByUser = ServiceUserDetail.from(energyPortalUserDto);

    assertThat(consentIssuingApprovalService.getConsentIssuingApprovalSummaryView(application))
        .contains(ConsentIssuingApprovalSummaryView.from(consentIssuingApproval, approvedByUser));
  }

  @Test
  void deleteConsentIssuingApproval_whenConsentIssuingApprovalFound_thenItIsDeleted() {
    var consentIssuingApproval = ConsentIssuingApprovalTestUtil.newBuilder().build();

    when(consentIssuingApprovalRepository.findByApplicationId(application.getId()))
        .thenReturn(Optional.of(consentIssuingApproval));

    consentIssuingApprovalService.deleteConsentIssuingApproval(application);

    verify(consentIssuingApprovalRepository).delete(consentIssuingApproval);
  }

  @Test
  void deleteConsentIssuingApproval_whenConsentIssuingApprovalNotFound_thenThrowEntityNotFoundException() {
    when(consentIssuingApprovalRepository.findByApplicationId(application.getId()))
        .thenReturn(Optional.empty());

    var exception = Assertions.assertThrows(
        EntityNotFoundException.class,
        () -> consentIssuingApprovalService.deleteConsentIssuingApproval(application)
    );

    Assertions.assertEquals("Consent issuing approval not found for application with id: 1",
        exception.getMessage());
  }
}
