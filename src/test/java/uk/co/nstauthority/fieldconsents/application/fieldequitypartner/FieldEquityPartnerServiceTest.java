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
import java.util.Optional;
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
import uk.co.fivium.energyportalapi.generated.client.FieldProjectionRoot;
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
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamScopeReference;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class FieldEquityPartnerServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldApi fieldApi;

  @Mock
  private TeamQueryService teamQueryService;

  @Spy
  @InjectMocks
  private FieldEquityPartnerService fieldEquityPartnerService;

  private ApplicationVersion applicationVersion;

  private List<ApplicationAsset> applicationAssets;

  private List<Integer> fieldIds;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    applicationAssets = List.of(
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
    fieldIds = applicationAssets.stream().map(ApplicationAsset::getAssetId).toList();
  }

  @Test
  void getFieldEquityPartnersView_byApplicationVersion() {
    var fields = List.of(
        getFieldWithFieldEquityPartner("aaa"),
        getFieldWithFieldEquityPartner("b"),
        getFieldWithFieldEquityPartner("cc")
    );

    var formattedFieldEquityPartners = fields.stream()
        .map(Field::getFieldEquityPartners)
        .flatMap(List::stream)
        .map(FormattedFieldEquityPartner::from)
        .toList();

    var organisationGroupsWithoutConsentRecipients = List.of("a", "b");

    doReturn(fields).when(fieldEquityPartnerService).getFieldsWithFieldEquityPartners(applicationVersion);
    doReturn(formattedFieldEquityPartners).when(fieldEquityPartnerService).getFormattedFieldEquityPartners(fields);
    doReturn(organisationGroupsWithoutConsentRecipients).when(fieldEquityPartnerService).getOrganisationGroupNamesWithoutConsentRecipients(fields);

    var actualFieldEquityPartnersView = fieldEquityPartnerService.getFieldEquityPartnersView(applicationVersion);
    var expectedFieldEquityPartnersView = FieldEquityPartnersViewTestUtil.newBuilder()
        .withFormattedFieldEquityPartners(List.of(
            FormattedFieldEquityPartnerTestUtil.newBuilder()
                .withOrganisationUnitName("aaa")
                .withRegisteredNumber("3")
                .build(),
            FormattedFieldEquityPartnerTestUtil.newBuilder()
                .withOrganisationUnitName("b")
                .withRegisteredNumber("1")
                .build(),
            FormattedFieldEquityPartnerTestUtil.newBuilder()
                .withOrganisationUnitName("cc")
                .withRegisteredNumber("2")
                .build()
        ))
        .withOrganisationGroupsWithoutConsentRecipients(organisationGroupsWithoutConsentRecipients)
        .build();

    assertThat(actualFieldEquityPartnersView).isEqualTo(expectedFieldEquityPartnersView);
  }

  @Test
  void getFormattedFieldEquityPartners_byApplicationVersion() {
    var fields = List.of(
        getFieldWithFieldEquityPartner("b"),
        getFieldWithFieldEquityPartner("c"),
        getFieldWithFieldEquityPartner("a")
    );

    var formattedFieldEquityPartners = fields
        .stream()
        .map(Field::getFieldEquityPartners)
        .flatMap(List::stream)
        .map(FormattedFieldEquityPartner::from)
        .toList();

    doReturn(fields).when(fieldEquityPartnerService).getFieldsWithFieldEquityPartners(applicationVersion);
    doReturn(formattedFieldEquityPartners).when(fieldEquityPartnerService).getFormattedFieldEquityPartners(fields);

    assertThat(fieldEquityPartnerService.getFormattedFieldEquityPartners(applicationVersion)).isEqualTo(formattedFieldEquityPartners);
  }

  @Test
  void getFieldsWithFieldEquityPartners_withApplicationVersion() {
    var fields = List.of(
        getFieldWithFieldEquityPartner("a"),
        getFieldWithFieldEquityPartner("b"),
        getFieldWithFieldEquityPartner("c")
    );

    when(applicationAssetService.findAssetsByApplicationVersionAndAssetTypeAndAssetRoles(
        applicationVersion,
        AssetType.FIELD,
        EnumSet.of(AssetRole.PRIMARY, AssetRole.SECONDARY)
    )).thenReturn(applicationAssets);

    doReturn(fields).when(fieldEquityPartnerService).getFieldsWithFieldEquityPartners(fieldIds);

    assertThat(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion)).isEqualTo(fields);
  }

  @Test
  void getFieldsWithFieldEquityPartners_withFieldIds() {
    var fields = List.of(
        getFieldWithFieldEquityPartner("a"),
        getFieldWithFieldEquityPartner("b"),
        getFieldWithFieldEquityPartner("c")
    );

    when(fieldApi.getFieldsByIds(eq(fieldIds), any(FieldsProjectionRoot.class), any(RequestPurpose.class))).thenReturn(fields);

    assertThat(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(fieldIds)).isEqualTo(fields);
  }

  @Test
  void getFieldWithFieldEquityPartners_fieldDoesNotExist() {
    var fieldId = 1;

    when(fieldApi.findFieldById(eq(fieldId), any(FieldProjectionRoot.class), any(RequestPurpose.class)))
        .thenReturn(Optional.empty());

    assertThat(fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)).isEmpty();
  }

  @Test
  void getFieldWithFieldEquityPartners_fieldExists() {
    var fieldId = 1;

    var field = getFieldWithFieldEquityPartner("a");

    when(fieldApi.findFieldById(eq(fieldId), any(FieldProjectionRoot.class), any(RequestPurpose.class)))
        .thenReturn(Optional.of(field));

    assertThat(fieldEquityPartnerService.getFieldWithFieldEquityPartners(fieldId)).contains(field);
  }

  @Test
  void getFormattedFieldEquityPartners_withoutPrimaryField() {
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
  void getFormattedFieldEquityPartners_byFields() {
    var fields = List.of(
        getFieldWithFieldEquityPartner("b"),
        getFieldWithFieldEquityPartner("cc"),
        getFieldWithFieldEquityPartner("aaa"),
        getFieldWithFieldEquityPartner("aaa")
    );

    assertThat(fieldEquityPartnerService.getFormattedFieldEquityPartners(fields)).containsExactly(
        new FormattedFieldEquityPartner("aaa", "3"),
        new FormattedFieldEquityPartner("b", "1"),
        new FormattedFieldEquityPartner("cc", "2")
    );
  }

  @Test
  void getOrganisationGroupNamesWithoutConsentRecipients() {
    var org1Id = 1;
    var org2Id = 2;
    var org3Id = 3;

    var team1 = TeamTestUtil.newBuilder()
        .withScopeId(String.valueOf(org1Id))
        .withName("FIRST")
        .build();

    var team2 = TeamTestUtil.newBuilder()
        .withScopeId(String.valueOf(org2Id))
        .withName("FIRST")
        .build();

    var team3 = TeamTestUtil.newBuilder()
        .withScopeId(String.valueOf(org3Id))
        .withName("FIRST")
        .build();

    var fields = List.of(
        getFieldWithOrganisationGroups(Map.of(
            org1Id, team1.getName(),
            org2Id, team2.getName()
        )),
        getFieldWithOrganisationGroups(Map.of(
            org1Id, team1.getName()
        )),
        getFieldWithOrganisationGroups(Map.of(
            org2Id, team2.getName(),
            org3Id, team3.getName()
        ))
    );

    var teamScopeIds = Set.of(
        String.valueOf(team1.getScopeId()),
        String.valueOf(team2.getScopeId()),
        String.valueOf(team3.getScopeId())
    );

    var teamRoles = List.of(
        // team 1 has a consent recipient
        TeamRoleTestUtil.newBuilder()
            .withTeam(team1)
            .withRole(Role.CONSENT_RECIPIENT)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(team1)
            .withRole(Role.ACCESS_MANAGER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(team1)
            .withRole(Role.CREATOR)
            .build(),
        // team 3 also has a consent recipient
        TeamRoleTestUtil.newBuilder()
            .withTeam(team3)
            .withRole(Role.CREATOR)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(team3)
            .withRole(Role.EDITOR)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(team3)
            .withRole(Role.ACCESS_MANAGER)
            .build(),
        TeamRoleTestUtil.newBuilder()
            .withTeam(team3)
            .withRole(Role.CONSENT_RECIPIENT)
            .build(),
        // team 2 does not have a consent recipient
        TeamRoleTestUtil.newBuilder()
            .withTeam(team2)
            .withRole(Role.SUBMITTER)
            .build()
    );

    when(teamQueryService.getTeamRoles(TeamType.INDUSTRY, TeamScopeReference.ORGANISATION_GROUP_ID, teamScopeIds))
        .thenReturn(teamRoles);

    assertThat(fieldEquityPartnerService.getOrganisationGroupNamesWithoutConsentRecipients(fields))
        .containsExactly(team2.getName());
  }

  private Field getFieldWithFieldEquityPartner(String fieldEquityPartnerName) {
    var fieldEquityPartners = List.of(
        FieldEquityPartner.newBuilder()
            .organisationUnit(OrganisationUnit.newBuilder()
                .name(fieldEquityPartnerName)
                .registeredNumber(String.valueOf(fieldEquityPartnerName.length()))
                .build())
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
