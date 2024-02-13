package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.field.FieldApi;
import uk.co.fivium.energyportalapi.generated.client.FieldsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.industry.IndustryTeamRole;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldApi fieldApi;

  @Mock
  private TeamService teamService;

  @Mock
  private TeamMemberService teamMemberService;

  @Spy
  @InjectMocks
  private FieldEquityPartnerService fieldEquityPartnerService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getFieldEquityPartnersView_byApplicationVersion() {
    var fieldEquityPartnerNames = List.of("a", "b", "c");
    var organisationGroupsWithoutConsentRecipients = List.of("a", "b");

    var fields = List.of(
        getFieldWithFieldEquityPartnerName("a"),
        getFieldWithFieldEquityPartnerName("b"),
        getFieldWithFieldEquityPartnerName("c")
    );

    doReturn(fields).when(fieldEquityPartnerService).getFieldsWithFieldEquityPartners(applicationVersion);
    doReturn(fieldEquityPartnerNames).when(fieldEquityPartnerService).getFieldEquityPartnerNames(fields);
    doReturn(organisationGroupsWithoutConsentRecipients).when(fieldEquityPartnerService).getOrganisationGroupNamesWithoutConsentRecipients(fields);

    var actualFieldEquityPartnersView = fieldEquityPartnerService.getFieldEquityPartnersView(applicationVersion);
    var expectedFieldEquityPartnersView = FieldEquityPartnersViewTestUtil.newBuilder()
        .withFieldEquityPartnerNames(fieldEquityPartnerNames)
        .withOrganisationGroupsWithoutConsentRecipients(organisationGroupsWithoutConsentRecipients)
        .build();

    assertThat(actualFieldEquityPartnersView).isEqualTo(expectedFieldEquityPartnersView);
  }

  @Test
  void getFieldEquityPartnerNames_byApplicationVersion() {
    var fields = List.of(
        getFieldWithFieldEquityPartnerName("b"),
        getFieldWithFieldEquityPartnerName("c"),
        getFieldWithFieldEquityPartnerName("a")
    );
    var fieldEquityPartnerNames = List.of("a", "b", "c");

    doReturn(fields).when(fieldEquityPartnerService).getFieldsWithFieldEquityPartners(applicationVersion);
    doReturn(fieldEquityPartnerNames).when(fieldEquityPartnerService).getFieldEquityPartnerNames(fields);

    assertThat(fieldEquityPartnerService.getFieldEquityPartnerNames(applicationVersion)).isEqualTo(fieldEquityPartnerNames);
  }

  @Test
  void getFieldsWithFieldEquityPartners() {
    var applicationAssets = List.of(
        ApplicationAssetTestUtil.newBuilder()
            .withAssetId(1)
            .withAssetType(AssetType.FIELD)
            .withAssetRole(AssetRole.PRIMARY)
            .build(),
        ApplicationAssetTestUtil.newBuilder()
            .withAssetId(2)
            .withAssetType(AssetType.FIELD)
            .withAssetRole(AssetRole.SECONDARY)
            .build(),
        ApplicationAssetTestUtil.newBuilder()
            .withAssetId(3)
            .withAssetType(AssetType.FIELD)
            .withAssetRole(AssetRole.SECONDARY)
            .build()
    );
    var fieldIds = applicationAssets.stream().map(ApplicationAsset::getAssetId).toList();

    var fields = List.of(
        getFieldWithFieldEquityPartnerName("a"),
        getFieldWithFieldEquityPartnerName("b"),
        getFieldWithFieldEquityPartnerName("c")
    );

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
        applicationVersion,
        AssetType.FIELD,
        EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    )).thenReturn(applicationAssets);

    when(fieldApi.getFieldsByIds(eq(fieldIds), any(FieldsProjectionRoot.class), any(RequestPurpose.class))).thenReturn(fields);

    assertThat(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)).isEqualTo(fields);
  }

  @Test
  void getFieldsWithFieldEquityPartners_withoutPrimaryField() {
    var nonPrimaryApplicationAssets = List.of(
        ApplicationAssetTestUtil.newBuilder()
            .withAssetId(1)
            .withAssetType(AssetType.FIELD)
            .withAssetRole(AssetRole.SECONDARY)
            .build()
    );

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
        applicationVersion,
        AssetType.FIELD,
        EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    )).thenReturn(nonPrimaryApplicationAssets);

    assertThatThrownBy(() -> fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion))
        .isInstanceOf(IllegalStateException.class);
  }

  @Test
  void getFieldEquityPartnerNames_byFields() {
    var fields = List.of(
        getFieldWithFieldEquityPartnerName("b"),
        getFieldWithFieldEquityPartnerName("c"),
        getFieldWithFieldEquityPartnerName("a"),
        getFieldWithFieldEquityPartnerName("a")
    );

    assertThat(fieldEquityPartnerService.getFieldEquityPartnerNames(fields)).containsExactly("a", "b", "c");
  }

  @Test
  void getOrganisationGroupNamesWithoutConsentRecipients() {
    var org1Id = 1;
    var org2Id = 2;
    var org3Id = 3;

    var team1 = TeamTestUtil.Builder().withOrganisationGroupId(org1Id).withDisplayName("FIRST").build();
    var team2 = TeamTestUtil.Builder().withOrganisationGroupId(org2Id).withDisplayName("SECOND").build();
    var team3 = TeamTestUtil.Builder().withOrganisationGroupId(org3Id).withDisplayName("THIRD").build();
    var teams = List.of(team1, team2);

    var fields = List.of(
        getFieldWithOrganisationGroups(Map.of(
            org1Id, team1.getDisplayName(),
            org2Id, team2.getDisplayName()
        )),
        getFieldWithOrganisationGroups(Map.of(
            org1Id, team1.getDisplayName()
        )),
        getFieldWithOrganisationGroups(Map.of(
            org2Id, team2.getDisplayName(),
            org3Id, team3.getDisplayName()
        ))
    );

    var organisationGroupIds = Set.of(org1Id, org2Id, org3Id);

    var teamsWithConsentRecipients = Set.of(team1, team3);

    when(teamService.getTeamsByOrganisationGroupIds(organisationGroupIds)).thenReturn(teams);
    when(teamMemberService.getTeamsWhereMemberExistsWithRole(teams, IndustryTeamRole.CONSENT_RECIPIENT)).thenReturn(teamsWithConsentRecipients);

    assertThat(fieldEquityPartnerService.getOrganisationGroupNamesWithoutConsentRecipients(fields))
        .containsExactly(team2.getDisplayName());
  }

  private Field getFieldWithFieldEquityPartnerName(String fieldEquityPartnerName) {
    var fieldEquityPartners = List.of(
        FieldEquityPartner.newBuilder()
            .organisationUnit(OrganisationUnit.newBuilder().name(fieldEquityPartnerName).build())
            .build()
    );

    return Field.newBuilder()
        .fieldEquityPartners(fieldEquityPartners)
        .build();
  }

  private Field getFieldWithOrganisationGroups(Map<Integer, String> organisationGroupNameById) {
    var organisationGroups = organisationGroupNameById.entrySet().stream()
        .map(entry -> OrganisationGroup.newBuilder()
            .organisationGroupId(entry.getKey())
            .name(entry.getValue())
            .build()
        )
        .toList();

    return Field.newBuilder()
        .fieldEquityPartners(List.of(
            FieldEquityPartner.newBuilder()
                .organisationUnit(OrganisationUnit.newBuilder()
                    .organisationGroups(organisationGroups)
                    .build())
                .build()
        ))
        .build();
  }

}
