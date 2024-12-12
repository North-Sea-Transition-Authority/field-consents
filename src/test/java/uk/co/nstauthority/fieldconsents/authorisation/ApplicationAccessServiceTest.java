package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ApplicationAccessServiceTest {

  @Mock
  private OrganisationUnitPermissionService organisationUnitPermissionService;

  @Mock
  private ConsultationService consultationService;

  @Mock
  private FieldEquityPartnerAccessService fieldEquityPartnerAccessService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private ApplicationAccessService applicationAccessService;

  private final ServiceUserDetail user = ServiceUserDetailTestUtil.Builder().build();
  private final ApplicationVersion applicationVersion = new ApplicationVersion();

  @BeforeEach
  void setUp() {
    applicationVersion.setPrimaryOperatorOuId(1);
    applicationVersion.setApplication(new Application());
  }

  @Test
  void userHasAnyIndustryRole_fieldApplication_isFieldEquityPartner() {
    var operatorRoles = Set.of(Role.CREATOR, Role.SUBMITTER);
    var fieldEquityPartnerRole = Role.CONSENT_RECIPIENT;

    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetType(AssetType.FIELD);

    when(organisationUnitPermissionService.getUserRolesForOperator(user, applicationVersion))
        .thenReturn(operatorRoles);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(applicationAsset);

    when(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, applicationVersion))
        .thenReturn(true);

    assertThat(applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, Set.of(fieldEquityPartnerRole))).isTrue();
    assertThat(applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, operatorRoles)).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_fieldApplication_isNotFieldEquityPartner() {
    var operatorRoles = Set.of(Role.CREATOR, Role.SUBMITTER);
    var fieldEquityPartnerRole = Role.CONSENT_RECIPIENT;

    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetType(AssetType.FIELD);

    when(organisationUnitPermissionService.getUserRolesForOperator(user, applicationVersion))
        .thenReturn(operatorRoles);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(applicationAsset);

    when(fieldEquityPartnerAccessService.userIsFieldEquityPartner(user, applicationVersion))
        .thenReturn(false);

    assertThat(applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, Set.of(fieldEquityPartnerRole))).isFalse();
    assertThat(applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, operatorRoles)).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_isOperatorFieldEquityPartner() {
    var operatorRoles = Set.of(Role.CREATOR, Role.SUBMITTER, Role.CONSENT_RECIPIENT);

    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetType(AssetType.FIELD);

    when(organisationUnitPermissionService.getUserRolesForOperator(user, applicationVersion))
        .thenReturn(operatorRoles);

    assertThat(applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, operatorRoles)).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_terminalApplication() {
    var operatorRoles = Set.of(Role.CREATOR, Role.SUBMITTER);
    var applicationAsset = new ApplicationAsset();
    applicationAsset.setAssetType(AssetType.TERMINAL);

    when(organisationUnitPermissionService.getUserRolesForOperator(user, applicationVersion))
        .thenReturn(operatorRoles);

    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(applicationAsset);

    assertThat(applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, operatorRoles)).isTrue();
  }

  @Test
  void userHasAnyIndustryRole_nonIndustryRolePassedIn() {
    // Role.CASE_MANAGER is an invalid industry role
    var roles = Set.of(Role.CONSENT_RECIPIENT, Role.VIEWER, Role.CASE_MANAGER);
    assertThatThrownBy(() -> applicationAccessService.userHasAnyIndustryRole(user, applicationVersion, roles))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void userHasAnyConsulteeRole_consultationExistsForApplication() {
    var consultation = new Consultation();
    var consulteeRoles = Set.of(Role.ALLOCATOR);

    when(consultationService.getConsultationsByApplication(applicationVersion.getApplication()))
        .thenReturn(List.of(consultation));

    when(teamQueryService.getStaticRoles(user, TeamType.CONSULTEE))
        .thenReturn(consulteeRoles);

    assertThat(applicationAccessService.userHasAnyConsulteeRole(user, applicationVersion, consulteeRoles)).isTrue();
  }

  @Test
  void userHasAnyConsulteeRole_consultationDoesNotExistForApplication() {
    assertThat(applicationAccessService.userHasAnyConsulteeRole(user, applicationVersion, Set.of(Role.ALLOCATOR))).isFalse();
  }

  @Test
  void userHasAnyConsulteeRole_nonConsulteeRolePassedIn() {
    // Role.EDITOR is an invalid consultee role
    var roles = Set.of(Role.ALLOCATOR, Role.RESPONDER, Role.EDITOR);
    assertThatThrownBy(() -> applicationAccessService.userHasAnyConsulteeRole(user, applicationVersion, roles))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
