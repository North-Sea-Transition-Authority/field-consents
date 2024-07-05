package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_REFERENCE;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService.ALL_ORG_UNITS_DATA_ITEM_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService.FIELD_LOOKUP_PURPOSE;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUserAction.RESUME_APPLICATION;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUserAction.VIEW_APPLICATION;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.camUser;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.caseOfficer;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForField;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareSubmittedForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentAssignedToCamForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentAssignedToCaseOfficerForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.portalUserDtosMap;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataItemUtil.technicalReviewer;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.util.SelfReturningAnswer;

@ExtendWith(MockitoExtension.class)
class ApplicationDataItemDtoServiceTest {

  private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @Mock
  private FieldService fieldService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  @Mock
  private ApplicationService applicationService;

  @Mock
  private ApplicationDataItemViewQueryService applicationDataItemQueryService;

  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @BeforeEach
  void setUp() {
    applicationDataItemDtoService = spy(new ApplicationDataItemDtoService(
        fieldService,
        energyPortalUserService,
        organisationUnitService,
        applicationService,
        applicationDataItemQueryService,
        clock
    ));
  }

  @Test
  void getOrganisationUnitJsonsFromApplicationDataItemDtos_emptyList() {
    var applicationDataItemDtoTerminal = ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    when(organisationUnitService.getOrganisationUnitsByIds(List.of(PRIMARY_OPERATOR_OU_ID_1),
        ALL_ORG_UNITS_DATA_ITEM_PURPOSE))
        .thenReturn(Collections.emptyList());

    assertThat(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(
        applicationDataItemDtoTerminal)))
        .isEmpty();
  }

  @Test
  void getOrganisationUnitJsonsFromApplicationDataItemDtos_nonEmptyList() {
    var applicationDataItemDtoTerminal = ApplicationDataItemUtil.getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    when(organisationUnitService.getOrganisationUnitsByIds(List.of(PRIMARY_OPERATOR_OU_ID_1),
        ALL_ORG_UNITS_DATA_ITEM_PURPOSE))
        .thenReturn(List.of(OrganisationUnitTestUtil.orgUnit1Json));

    assertThat(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(List.of(
        applicationDataItemDtoTerminal)))
        .containsExactly(OrganisationUnitTestUtil.orgUnit1Json);
  }

  @Test
  void getFieldJsonMapFromApplicationDataItemDtos_emptyMap() {
    var applicationDataItemDtoField = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(Collections.emptyList());

    assertThat(
        applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDtoField)))
        .isEmpty();
  }

  @Test
  void getFieldJsonMapFromApplicationDataItemDtos_nonEmptyMap() {
    var applicationDataItemDtoField = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    when(fieldService.findFieldsByIds(List.of(FIELD_ID_1), FIELD_LOOKUP_PURPOSE)).thenReturn(
        List.of(field1JsonWithOperator));

    assertThat(
        applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(List.of(applicationDataItemDtoField)))
        .isEqualTo(Map.of(FIELD_ID_1, field1JsonWithOperator));
  }

  @Test
  void getEnergyPortalUserDtoMapFromApplicationDataItemDtos() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedToCaseOfficerForTerminal();
    var portalUserWuaIdList = List.of(
        WebUserAccountId.from(applicationDataItemDto.submittedByWuaId()),
        WebUserAccountId.from(applicationDataItemDto.caseOfficerWuaId()),
        WebUserAccountId.from(applicationDataItemDto.technicalReviewerWuaId())
    );

    when(energyPortalUserService.getEnergyPortalUserMap(portalUserWuaIdList))
        .thenReturn(portalUserDtosMap);

    assertThat(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(
        List.of(applicationDataItemDto)))
        .isEqualTo(portalUserDtosMap);
  }

  @Test
  void getDisplayReference_whenApplicationInProgressAndVersionNoNull_resume() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField(null);

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, RESUME_APPLICATION))
        .isEqualTo("Resume application");
  }

  @Test
  void getDisplayReference_whenApplicationInProgressAndVersionNoNull_view() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField(null);

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, VIEW_APPLICATION))
        .isEqualTo("View application");
  }

  @Test
  void getDisplayReference_whenApplicationInProgress_resume() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField(1);
    var reference = "VCON/500/0 (Version 1)";
    when(applicationService.generateApplicationReference(applicationDataItemDto)).thenReturn(reference);

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, RESUME_APPLICATION))
        .isEqualTo("Resume %s".formatted(reference));
  }

  @Test
  void getDisplayReference_whenApplicationInProgress_view() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField(1);
    var reference = "VCON/500/0 (Version 1)";
    when(applicationService.generateApplicationReference(applicationDataItemDto)).thenReturn(reference);

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, VIEW_APPLICATION))
        .isEqualTo("View %s".formatted(reference));
  }

  @Test
  void getDisplayReference_whenApplicationSubmitted() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentSubmittedForTerminal();
    when(applicationService.generateApplicationReference(applicationDataItemDto)).thenReturn("VCON/500/0 (Version 1)");

    assertThat(applicationDataItemDtoService.getDisplayReference(applicationDataItemDto, RESUME_APPLICATION))
        .isEqualTo("VCON/500/0 (Version 1)");
  }

  @Test
  void getDisplayConsentDuration_whenInProgressAndDurationNotYetSpecified() {
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForProductionInProgressForFieldNoDuration();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEmpty();
  }

  @Test
  void getDisplayConsentDuration_whenConsented() {
    var applicationDataItemDto = ApplicationDataItemUtil.getApplicationDataItemDtoForLongFlareConsentedForField();
    var consentDurationString = applicationDataItemDto.duration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo(
            "%s %s - %s".formatted(consentDurationString,
                DateUtils.format(applicationDataItemDto.consentStartDate(), DateUtils.SHORT_DATE),
                DateUtils.format(applicationDataItemDto.consentEndDate(), DateUtils.SHORT_DATE)
            )
        );
  }

  @Test
  void getDisplayConsentDuration_whenNotConsented_shortTerm() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedToCaseOfficerForTerminal();
    var consentDurationString = applicationDataItemDto.duration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo(
            "%s %s - %s".formatted(consentDurationString,
                DateUtils.format(applicationDataItemDto.shortTermStartDate(), DateUtils.SHORT_DATE),
                DateUtils.format(applicationDataItemDto.shortTermEndDate(), DateUtils.SHORT_DATE)
            )
        );
  }

  @Test
  void getDisplayConsentDuration_whenNotConsented_Annual() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();
    var consentDurationString = applicationDataItemDto.duration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo("%s %s".formatted(consentDurationString, applicationDataItemDto.consentYear()));
  }

  @Test
  void getDisplayConsentDuration_whenNotConsented_LongTerm() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForTerminal();
    var consentDurationString = applicationDataItemDto.duration().getShortDisplayName();

    assertThat(applicationDataItemDtoService.getDisplayConsentDuration(applicationDataItemDto))
        .isEqualTo(
            "%s %d - %d".formatted(consentDurationString, applicationDataItemDto.longTermStartYear(),
                applicationDataItemDto.longTermEndYear())
        );
  }

  @Test
  void getDisplayAssetLocation_Field() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayAssetLocation(applicationDataItemDto,
        Map.of(FIELD_ID_1, field1JsonWithOperator)))
        .isEqualTo(field1JsonWithOperator.getGeographicArea().getDisplayName());
  }


  @Test
  void getDisplayAssetLocation_Terminal() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayAssetLocation(applicationDataItemDto,
        Map.of(FIELD_ID_1, field1JsonWithOperator)))
        .isEmpty();
  }

  @Test
  void getDisplayAssetLocation_unknownArea() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayAssetLocation(applicationDataItemDto,
        Map.of(FIELD_ID_2, field2JsonWithOperator)))
        .isEqualTo("Unknown area");
  }

  @Test
  void getDisplayCaseOfficer_withNoCaseOfficer() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayCaseOfficer(applicationDataItemDto, portalUserDtosMap))
        .isEmpty();
  }

  @Test
  void getDisplayCaseOfficer_withCaseOfficer() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedToCaseOfficerForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayCaseOfficer(applicationDataItemDto, portalUserDtosMap))
        .isEqualTo(caseOfficer.displayName());
  }

  @Test
  void getDisplayCamUser_withIndustryType() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayCamUser(applicationDataItemDto, portalUserDtosMap,
        TeamType.INDUSTRY))
        .isEmpty();
  }

  @Test
  void getDisplayCamUser_withNoCamUserAssigned() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayCamUser(applicationDataItemDto, portalUserDtosMap,
        TeamType.REGULATOR))
        .isEmpty();
  }

  @Test
  void getDisplayCamUser_withCamUserAssigned() {
    var applicationDataItemDto = getApplicationDataItemDtoForShortVentAssignedToCamForTerminal();

    assertThat(applicationDataItemDtoService.getDisplayCamUser(applicationDataItemDto, portalUserDtosMap,
        TeamType.REGULATOR))
        .isEqualTo(camUser.displayName());
  }

  @Test
  void getDisplayTechnicalReviewer_withIndustryType() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayTechnicalReviewer(applicationDataItemDto, portalUserDtosMap,
        TeamType.INDUSTRY))
        .isEmpty();
  }

  @Test
  void getDisplayTechnicalReviewer_withNoTechnicalReviewer() {
    var applicationDataItemDto = getApplicationDataItemDtoForAnnualProductionInProgressForField();

    assertThat(applicationDataItemDtoService.getDisplayTechnicalReviewer(applicationDataItemDto, portalUserDtosMap,
        TeamType.REGULATOR))
        .isEmpty();
  }

  @Test
  void getDisplayTechnicalReviewer_withTechnicalReviewer() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForField();

    assertThat(applicationDataItemDtoService.getDisplayTechnicalReviewer(applicationDataItemDto, portalUserDtosMap,
        TeamType.REGULATOR))
        .isEqualTo(technicalReviewer.displayName());
  }

  @Test
  void getApplicationDataItem() {
    var applicationDataItemDto = getApplicationDataItemDtoForLongFlareSubmittedForField();
    var serviceUserDetail = ServiceUserDetailTestUtil.Builder()
        .withWuaId(1L)
        .withPersonId(1L)
        .build();
    var energyPortalUserDto = new EnergyPortalUserDto(
        serviceUserDetail.wuaId(),
        serviceUserDetail.personId(),
        "",
        serviceUserDetail.forename(),
        serviceUserDetail.surname(),
        serviceUserDetail.emailAddress(),
        "",
        false,
        true
    );

    var organisationUnitNameById = Map.of(1, "org");
    var fieldJsonById = Map.of(FIELD_ID_1, field1Json);
    var portalUserDtoByWuaId = Map.of(WebUserAccountId.from(serviceUserDetail), energyPortalUserDto);

    when(applicationService.generateApplicationReference(applicationDataItemDto)).thenReturn(APPLICATION_REFERENCE);

    var expectedApplicationDataItem = new ApplicationDataItemView(
        applicationDataItemDto.applicationId(),
        applicationDataItemDto.type().getDisplayName(),
        "%s %d - %d".formatted(
            ConsentLengthType.LONG_TERM.getShortDisplayName(),
            applicationDataItemDto.longTermStartYear(),
            applicationDataItemDto.longTermEndYear()
        ),
        APPLICATION_REFERENCE,
        "org",
        applicationDataItemDto.assetName(),
        "Central North Sea",
        applicationDataItemDto.status().getDisplayName(),
        DateUtils.format(applicationDataItemDto.submittedDateTime(), DateUtils.DATE_TIME),
        energyPortalUserDto.displayName(),
        true,
        "ACE: Yes",
        "",
        "",
        false,
        "",
        null,
        null,
        false,
        "",
        null,
        null,
        null,
        "P1, P2, P3",
        null,
        null
    );

    assertThat(applicationDataItemDtoService.getApplicationDataItemView(
        applicationDataItemDto,
        VIEW_APPLICATION,
        TeamType.INDUSTRY,
        organisationUnitNameById,
        fieldJsonById,
        portalUserDtoByWuaId
    )).isEqualTo(expectedApplicationDataItem);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getApplicationDataItem_submitted_industry_doesNotSeeConsultationsTag(boolean consultationOpen) {
    var dto = mock(ApplicationDataItemDto.class);
    when(dto.type()).thenReturn(ApplicationType.PRODUCTION);
    when(dto.status()).thenReturn(ApplicationVersionStatus.SUBMITTED);
    when(dto.withdrawalOpen()).thenReturn(false);
    when(dto.applicationUpdateOpen()).thenReturn(false);
    when(dto.consultationOpen()).thenReturn(consultationOpen);

    var teamType = TeamType.INDUSTRY;
    mockGetDisplayMethodCalls(dto, RESUME_APPLICATION, teamType);

    assertThat(applicationDataItemDtoService.getApplicationDataItemView(
        dto,
        RESUME_APPLICATION,
        teamType,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap()
    ))
        .extracting(ApplicationDataItemView::consultationOpen, ApplicationDataItemView::consultationDeadline)
        .containsOnlyNulls();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getApplicationDataItem_submitted_opred_doesNotSeeApplicationUpdateTag(boolean applicationUpdateOpen) {
    var dto = mock(ApplicationDataItemDto.class);
    when(dto.type()).thenReturn(ApplicationType.PRODUCTION);
    when(dto.status()).thenReturn(ApplicationVersionStatus.SUBMITTED);
    when(dto.withdrawalOpen()).thenReturn(false);
    when(dto.applicationUpdateOpen()).thenReturn(applicationUpdateOpen);
    when(dto.consultationOpen()).thenReturn(false);

    var teamType = TeamType.OPRED;
    mockGetDisplayMethodCalls(dto, VIEW_APPLICATION, teamType);

    assertThat(applicationDataItemDtoService.getApplicationDataItemView(
        dto,
        VIEW_APPLICATION,
        teamType,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap()
    ))
        .extracting(ApplicationDataItemView::applicationUpdateOpen, ApplicationDataItemView::applicationUpdateDeadline)
        .containsOnlyNulls();
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getApplicationDataItem_submitted_opred_doesNotSeeWithdrawalTag(boolean applicationWithdrawalOpen) {
    var dto = mock(ApplicationDataItemDto.class);
    when(dto.type()).thenReturn(ApplicationType.PRODUCTION);
    when(dto.status()).thenReturn(ApplicationVersionStatus.SUBMITTED);
    when(dto.withdrawalOpen()).thenReturn(applicationWithdrawalOpen);
    when(dto.applicationUpdateOpen()).thenReturn(false);
    when(dto.consultationOpen()).thenReturn(false);

    var teamType = TeamType.OPRED;
    mockGetDisplayMethodCalls(dto, VIEW_APPLICATION, teamType);

    assertThat(applicationDataItemDtoService.getApplicationDataItemView(
        dto,
        VIEW_APPLICATION,
        teamType,
        Collections.emptyMap(),
        Collections.emptyMap(),
        Collections.emptyMap()
    ))
        .extracting(ApplicationDataItemView::withdrawalOpen)
        .isNull();
  }


  private void mockGetDisplayMethodCalls(
      ApplicationDataItemDto dto,
      ApplicationDataItemUserAction userAction,
      TeamType teamType
  ) {
    doReturn("").when(applicationDataItemDtoService).getDisplayConsentDuration(dto);
    doReturn("").when(applicationDataItemDtoService).getDisplayReference(dto, userAction);
    doReturn("").when(applicationDataItemDtoService).getOperator(dto, Collections.emptyMap());
    doReturn("").when(applicationDataItemDtoService).getDisplayAssetLocation(dto, Collections.emptyMap());
    doReturn("").when(applicationDataItemDtoService).getSubmittedDateTime(dto);
    doReturn("").when(applicationDataItemDtoService).getSubmittedByName(dto, Collections.emptyMap());
    doReturn("").when(applicationDataItemDtoService).getDisplayCaseOfficer(dto, Collections.emptyMap());
    doReturn("").when(applicationDataItemDtoService).getDisplayCamUser(dto, Collections.emptyMap(), teamType);
    doReturn("").when(applicationDataItemDtoService).getDisplayTechnicalReviewer(dto, Collections.emptyMap(), teamType);
    doReturn("").when(applicationDataItemDtoService).getApplicationUpdateDeadline(dto);
    doReturn("").when(applicationDataItemDtoService).getConsultationDeadline(dto);
  }

  @Test
  void getSubmittedDateTime_whenSubmittedDateTimeExists() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.submittedDateTime()).thenReturn(Instant.now());

    assertThat(applicationDataItemDtoService.getSubmittedDateTime(dataItemDto))
        .isEqualTo(DateUtils.format(dataItemDto.submittedDateTime(), DateUtils.DATE_TIME));
  }

  @Test
  void getSubmittedDateTime_whenSubmittedDateTimeDoesNotExist() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.submittedDateTime()).thenReturn(null);

    assertThat(applicationDataItemDtoService.getSubmittedDateTime(dataItemDto)).isEmpty();
  }

  @Test
  void getSubmittedByName_whenSubmitterExists() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.submittedByWuaId()).thenReturn(1L);

    var energyPortalUserDto = mock(EnergyPortalUserDto.class);
    when(energyPortalUserDto.displayName()).thenReturn("energyPortalUser");

    var portalUserDtoByWuaId = Map.of(new WebUserAccountId(1L), energyPortalUserDto);

    assertThat(applicationDataItemDtoService.getSubmittedByName(dataItemDto, portalUserDtoByWuaId))
        .isEqualTo("energyPortalUser");
  }

  @Test
  void getSubmittedByName_whenSubmitterDoesNotExist() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.submittedByWuaId()).thenReturn(null);

    assertThat(applicationDataItemDtoService.getSubmittedByName(dataItemDto, Collections.emptyMap())).isEmpty();
  }

  @Test
  void getTechnicalReviewDeadline_reviewOpen() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.technicalReviewOpen()).thenReturn(true);
    when(dataItemDto.technicalReviewDeadline()).thenReturn(Instant.now());

    assertThat(applicationDataItemDtoService.getTechnicalReviewDeadline(dataItemDto))
        .isEqualTo(DateUtils.format(dataItemDto.technicalReviewDeadline(), DateUtils.DATE_TIME));
  }

  @Test
  void getTechnicalReviewDeadline_reviewNotOpen() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.technicalReviewOpen()).thenReturn(false);

    assertThat(applicationDataItemDtoService.getTechnicalReviewDeadline(dataItemDto)).isEmpty();
  }

  @Test
  void getConsultationDeadline_consultationOpen() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.consultationOpen()).thenReturn(true);
    when(dataItemDto.consultationDeadline()).thenReturn(Instant.now());

    assertThat(applicationDataItemDtoService.getConsultationDeadline(dataItemDto))
        .isEqualTo(DateUtils.format(dataItemDto.consultationDeadline(), DateUtils.DATE_TIME));
  }

  @Test
  void getConsultationDeadline_consultationNotOpen() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.consultationOpen()).thenReturn(false);

    assertThat(applicationDataItemDtoService.getConsultationDeadline(dataItemDto)).isEmpty();
  }

  @Test
  void getOperator_operatorExists() {
    var organisationUnitNameById = Map.of(1, "org name");

    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.operatorId()).thenReturn(1);

    assertThat(applicationDataItemDtoService.getOperator(dataItemDto, organisationUnitNameById)).isEqualTo("org name");
  }

  @Test
  void getOperator_operatorDoesNotExist() {
    var dataItemDto = mock(ApplicationDataItemDto.class);
    when(dataItemDto.operatorId()).thenReturn(1);

    assertThat(applicationDataItemDtoService.getOperator(dataItemDto, Collections.emptyMap())).isEqualTo(
        "MISSING OPERATOR");
  }

  @Test
  void getConsentStatus_consentNotIssued() {
    var dataItemDto = mock(ApplicationDataItemDto.class);

    when(dataItemDto.consentIssued()).thenReturn(false);

    assertThat(applicationDataItemDtoService.getConsentStatus(dataItemDto)).isNull();
  }

  @Test
  void getConsentStatus_consentIssued() {
    var dataItemDto = mock(ApplicationDataItemDto.class);

    var today = LocalDate.now(clock);

    when(dataItemDto.consentIssued()).thenReturn(true);
    when(dataItemDto.consentStartDate()).thenReturn(today);
    when(dataItemDto.consentEndDate()).thenReturn(today.plusDays(1));
    when(dataItemDto.consentSuperseded()).thenReturn(false);

    assertThat(applicationDataItemDtoService.getConsentStatus(dataItemDto)).isEqualTo(ConsentStatus.ACTIVE);
  }

  @Test
  void removeTagsForTeamType_industry() {
    var builder = mock(ApplicationDataItemView.Builder.class, new SelfReturningAnswer());

    applicationDataItemDtoService.removeTagsForTeamType(TeamType.INDUSTRY, builder);

    verifyTechnicalReviewDeadlineTagRemoved(builder);
    verify(builder).withConsultationOpen(null);
    verify(builder).withConsultationDeadline(null);
    verify(builder).withConsultationFurtherInformationOpen(null);
    verify(builder).withApprovedForIssue(null);

    verifyNoMoreInteractions(builder);
  }

  @Test
  void removeTagsForTeamType_opred() {
    var builder = mock(ApplicationDataItemView.Builder.class, new SelfReturningAnswer());

    applicationDataItemDtoService.removeTagsForTeamType(TeamType.OPRED, builder);

    verify(builder).withWithdrawalOpen(null);
    verifyTechnicalReviewDeadlineTagRemoved(builder);
    verifyApplicationUpdateTagRemoved(builder);
    verify(builder).withApprovedForIssue(null);

    verifyNoMoreInteractions(builder);
  }

  private void verifyTechnicalReviewDeadlineTagRemoved(ApplicationDataItemView.Builder builder) {
    verify(builder).withTechnicalReviewOpen(null);
    verify(builder).withTechnicalReviewDeadline(null);
  }

  private void verifyApplicationUpdateTagRemoved(ApplicationDataItemView.Builder builder) {
    verify(builder).withApplicationUpdateOpen(null);
    verify(builder).withApplicationUpdateDeadline(null);
  }

}
