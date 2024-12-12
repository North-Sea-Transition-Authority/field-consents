package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.ADDITIONAL_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.assertSummarySection;
import static uk.co.nstauthority.fieldconsents.application.summary.SummaryTestUtil.simpleSummaryCard;
import static uk.co.nstauthority.fieldconsents.application.summary.shared.AdditionalInformationSummarySectionService.FIELD_LOOKUP_PURPOSE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionService;
import uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyData;
import uk.co.nstauthority.fieldconsents.application.otherlegacydata.OtherLegacyDataSummaryService;
import uk.co.nstauthority.fieldconsents.application.supportinginformation.SupportingInformationService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class AdditionalInformationSummarySectionServiceTest {

  private static final String EIA_SCREENING_DIRECTION_ITEM = "EIA screening direction";

  private static final String SUPPORTING_INFORMATION_ITEM = "Supporting information";

  private static final String OTHER_LEGACY_APPLICATION_DETAILS_ITEM = "Other legacy application details";

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();
  @Mock
  private SupportingInformationService supportingInformationService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @Mock
  private EiaDirectionService eiaDirectionService;

  @Mock
  private OtherLegacyDataSummaryService otherLegacyDataSummaryService;

  @Mock
  private TeamQueryService teamQueryService;

  @InjectMocks
  private AdditionalInformationSummarySectionService additionalInformationSummarySectionService;

  @Test
  void getSummarySection_production_regulator_offshore() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var otherLegacyData = new OtherLegacyData();
    var summaryCard = simpleSummaryCard;
    when(eiaDirectionService.getEiaDirectionSummaryCard(applicationVersion)).thenReturn(summaryCard);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion)).thenReturn(List.of(summaryCard, summaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getAssetId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));
    when(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .thenReturn(summaryCard);
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build())
            .withRole(Role.CASE_OFFICER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCard(EIA_SCREENING_DIRECTION_ITEM, summaryCard),
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, List.of(summaryCard, summaryCard)),
            SummaryItem.withCard(OTHER_LEGACY_APPLICATION_DETAILS_ITEM, summaryCard)
        );
  }

  @Test
  void getSummarySection_production_industry_offshore() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var otherLegacyData = new OtherLegacyData();
    var summaryCard = simpleSummaryCard;
    when(eiaDirectionService.getEiaDirectionSummaryCard(applicationVersion)).thenReturn(summaryCard);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion)).thenReturn(List.of(summaryCard, summaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getAssetId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));
    when(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .thenReturn(summaryCard);
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build())
            .withRole(Role.EDITOR)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCard(EIA_SCREENING_DIRECTION_ITEM, summaryCard),
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, List.of(summaryCard, summaryCard)),
            SummaryItem.withCard(OTHER_LEGACY_APPLICATION_DETAILS_ITEM, summaryCard)
        );
  }

  @Test
  void getSummarySection_production_consultee_offshore() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var otherLegacyData = new OtherLegacyData();
    var summaryCard = simpleSummaryCard;
    when(eiaDirectionService.getEiaDirectionSummaryCard(applicationVersion)).thenReturn(summaryCard);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset1);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset1.getAssetId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field1Json);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));
    when(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .thenReturn(summaryCard);
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of());

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCard(EIA_SCREENING_DIRECTION_ITEM, summaryCard),
            SummaryItem.withCard(OTHER_LEGACY_APPLICATION_DETAILS_ITEM, summaryCard)
        );
  }

  @Test
  void getSummarySection_production_regulator_onshore() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard, simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset2.getAssetId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field2Json);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build())
            .withRole(Role.CASE_OFFICER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, List.of(simpleSummaryCard, simpleSummaryCard))
        );
  }

  @Test
  void getSummarySection_production_industry_onshore() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard, simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset2.getAssetId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field2Json);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build())
            .withRole(Role.CREATOR)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, List.of(simpleSummaryCard, simpleSummaryCard))
        );
  }

  @Test
  void getSummarySection_production_consultee_onshore() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(fieldService.getField(ApplicationAssetTestUtil.fieldAsset2.getAssetId(), FIELD_LOOKUP_PURPOSE))
        .thenReturn(FieldTestUtil.field2Json);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of());

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems()).isEmpty();
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSummarySection_nonProduction_regulator_onshore(
      ApplicationType applicationType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var otherLegacyData = new OtherLegacyData();
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));
    when(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .thenReturn(simpleSummaryCard);
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build())
            .withRole(Role.CASE_MANAGER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, Collections.singletonList(simpleSummaryCard)),
            SummaryItem.withCard(OTHER_LEGACY_APPLICATION_DETAILS_ITEM, simpleSummaryCard)
        );
  }


  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSummarySection_nonProduction_industry_onshore(
      ApplicationType applicationType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var otherLegacyData = new OtherLegacyData();
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));
    when(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .thenReturn(simpleSummaryCard);
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build())
            .withRole(Role.SUBMITTER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, Collections.singletonList(simpleSummaryCard)),
            SummaryItem.withCard(OTHER_LEGACY_APPLICATION_DETAILS_ITEM, simpleSummaryCard)
        );
  }


  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSummarySection_nonProduction_consultee_onshore(
      ApplicationType applicationType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    var otherLegacyData = new OtherLegacyData();
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset2);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.of(otherLegacyData));
    when(otherLegacyDataSummaryService.getOtherLegacyDataSummaryCard(otherLegacyData))
        .thenReturn(simpleSummaryCard);
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.CONSULTEE).build())
            .withRole(Role.ALLOCATOR)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCard(OTHER_LEGACY_APPLICATION_DETAILS_ITEM, simpleSummaryCard)
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSummarySection_nonProduction_regulator_unknownShore(
      ApplicationType applicationType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset3);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build())
            .withRole(Role.CASE_MANAGER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, Collections.singletonList(simpleSummaryCard))
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSummarySection_nonProduction_industry_unknownShore(
      ApplicationType applicationType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset3);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build())
            .withRole(Role.VIEWER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, Collections.singletonList(simpleSummaryCard))
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "PRODUCTION", mode = EnumSource.Mode.EXCLUDE)
  void getSummarySection_nonProduction_consultee_unknownShore(
      ApplicationType applicationType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.fieldAsset3);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.CONSULTEE).build())
            .withRole(Role.RESPONDER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .isEmpty();
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_regulator_terminal(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.terminalAsset1);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.REGULATOR).build())
            .withRole(Role.CASE_MANAGER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, Collections.singletonList(simpleSummaryCard))
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_industry_terminal(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(supportingInformationService.getSupportingInformationSummaryCards(applicationVersion))
        .thenReturn(List.of(simpleSummaryCard));
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.terminalAsset1);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.INDUSTRY).build())
            .withRole(Role.EDITOR)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .containsExactly(
            SummaryItem.withCards(SUPPORTING_INFORMATION_ITEM, Collections.singletonList(simpleSummaryCard))
        );
  }

  @ParameterizedTest
  @EnumSource(ApplicationType.class)
  void getSummarySection_consultee_terminal(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(ApplicationAssetTestUtil.terminalAsset1);
    when(otherLegacyDataSummaryService.findOtherLegacyData(applicationVersion))
        .thenReturn(Optional.empty());
    when(teamQueryService.getTeamRoles(USER)).thenReturn(List.of(
        TeamRoleTestUtil.newBuilder()
            .withTeam(TeamTestUtil.newBuilder().withTeamType(TeamType.CONSULTEE).build())
            .withRole(Role.RESPONDER)
            .build()
    ));

    var summarySection = additionalInformationSummarySectionService.getSummarySection(applicationVersion, USER).orElseThrow();
    assertSummarySection(summarySection, ADDITIONAL_INFORMATION_DISPLAY_ORDER);
    assertThat(summarySection.summaryItems())
        .isEmpty();
  }
}
