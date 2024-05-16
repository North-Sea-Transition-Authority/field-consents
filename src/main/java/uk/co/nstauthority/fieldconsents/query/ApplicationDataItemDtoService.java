package uk.co.nstauthority.fieldconsents.query;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.PermissionService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

/**
 * Implements the common security rules used by both work-area and search screen for user accessibility.
 * This also includes the shared code used to retrieve ApplicationDataItemDto details for both work-area and search.
 */
@Service
public class ApplicationDataItemDtoService {

  public static final String ALL_ORG_UNITS_DATA_ITEM_PURPOSE =
      "All organisation units available to display in application data items";
  public static final String FIELD_LOOKUP_PURPOSE = "Lookup field for the application data item";

  private final FieldService fieldService;
  private final EnergyPortalUserService energyPortalUserService;
  private final OrganisationUnitService organisationUnitService;
  private final ApplicationService applicationService;
  private final ApplicationVersionService applicationVersionService;
  private final PermissionService permissionService;
  private final ApplicationDataItemViewQueryService applicationDataItemViewQueryService;
  private final Clock clock;

  ApplicationDataItemDtoService(
      FieldService fieldService,
      EnergyPortalUserService energyPortalUserService,
      OrganisationUnitService organisationUnitService,
      ApplicationService applicationService,
      ApplicationVersionService applicationVersionService,
      PermissionService permissionService,
      ApplicationDataItemViewQueryService applicationDataItemViewQueryService,
      Clock clock
  ) {
    this.fieldService = fieldService;
    this.energyPortalUserService = energyPortalUserService;
    this.organisationUnitService = organisationUnitService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.permissionService = permissionService;
    this.applicationDataItemViewQueryService = applicationDataItemViewQueryService;
    this.clock = clock;
  }

  public List<OrganisationUnitJson> getOrganisationUnitJsonsFromApplicationDataItemDtos(
      List<? extends ApplicationDataItemDto> applicationDataItemDtos) {
    return organisationUnitService.getOrganisationUnitsByIds(
        applicationDataItemDtos
            .stream()
            .map(ApplicationDataItemDto::operatorId)
            .toList(),
        ALL_ORG_UNITS_DATA_ITEM_PURPOSE);
  }

  Map<Integer, FieldJson> getFieldJsonMapFromApplicationDataItemDtos(
      Collection<? extends ApplicationDataItemDto> applicationDataItemDtos
  ) {
    var fieldJsons = fieldService.findFieldsByIds(applicationDataItemDtos
        .stream()
        .filter(dto -> dto.assetType() == AssetType.FIELD)
        .map(ApplicationDataItemDto::assetId)
        .distinct()
        .toList(), FIELD_LOOKUP_PURPOSE);

    return fieldJsons
        .stream()
        .collect(Collectors.toMap(
            FieldJson::getId,
            Function.identity())
        );
  }

  Map<WebUserAccountId, EnergyPortalUserDto> getEnergyPortalUserDtoMapFromApplicationDataItemDtos(
      Collection<? extends ApplicationDataItemDto> applicationDataItemDtos
  ) {

    var wuaIds = applicationDataItemDtos
        .stream()
        .flatMap(dataItem -> Stream.of(
            dataItem.submittedByWuaId(),
            dataItem.caseOfficerWuaId(),
            dataItem.technicalReviewerWuaId(),
            dataItem.camWuaId()
        ))
        .filter(Objects::nonNull)
        .map(WebUserAccountId::new)
        .distinct()
        .toList();

    return energyPortalUserService.getEnergyPortalUserMap(wuaIds);
  }

  String getDisplayReference(ApplicationDataItemDto dataItemDto, ApplicationDataItemUserAction userAction) {
    var applicationVersion = applicationVersionService.getApplicationVersionById(dataItemDto.applicationVersionId());

    if (dataItemDto.status() == ApplicationVersionStatus.IN_PROGRESS) {
      if (dataItemDto.applicationNo() == null) {
        return "%s application".formatted(userAction.getDisplayName());
      }
      return "%s %s".formatted(
          userAction.getDisplayName(),
          applicationService.generateApplicationReference(applicationVersion)
      );
    }

    return applicationService.generateApplicationReference(applicationVersion);
  }

  public ApplicationDataItemUserAction getApplicationDataItemUserActionFromUser(ServiceUserDetail user) {
    return permissionService.hasPermission(user, EnumSet.of(RolePermission.EDIT_FCS_APPLICATIONS))
        ? ApplicationDataItemUserAction.RESUME_APPLICATION
        : ApplicationDataItemUserAction.VIEW_APPLICATION;
  }

  String getDisplayConsentDuration(ApplicationDataItemDto dataItemDto) {
    var consentDuration = dataItemDto.duration();

    if (Objects.isNull(consentDuration)) {
      return "";
    }

    // if application is consented use the consent data start/end date
    if (Boolean.TRUE.equals(dataItemDto.consentIssued())) {
      return "%s %s - %s".formatted(
          consentDuration.getShortDisplayName(),
          DateUtils.format(dataItemDto.consentStartDate(), DateUtils.SHORT_DATE),
          DateUtils.format(dataItemDto.consentEndDate(), DateUtils.SHORT_DATE)
      );
    }

    // otherwise use the application form data
    return switch (consentDuration) {
      case ANNUAL -> "%s %d".formatted(consentDuration.getShortDisplayName(), dataItemDto.consentYear());
      case LONG_TERM -> "%s %d - %d".formatted(
          consentDuration.getShortDisplayName(),
          dataItemDto.longTermStartYear(),
          dataItemDto.longTermEndYear()
      );
      case SHORT_TERM -> "%s %s - %s".formatted(
          consentDuration.getShortDisplayName(),
          DateUtils.format(dataItemDto.shortTermStartDate(), DateUtils.SHORT_DATE),
          DateUtils.format(dataItemDto.shortTermEndDate(), DateUtils.SHORT_DATE)
      );
    };
  }

  String getDisplayAssetLocation(ApplicationDataItemDto dataItemDto, Map<Integer, FieldJson> fieldJsonsMap) {
    if (dataItemDto.assetType() != AssetType.FIELD) {
      return "";
    }

    var matchingFieldJson = fieldJsonsMap.get(dataItemDto.assetId());
    return matchingFieldJson != null
        ? matchingFieldJson.getGeographicArea().getDisplayName()
        : "Unknown area";
  }

  String getDisplayAceFlag(ApplicationDataItemDto dataItemDto) {
    if (Objects.isNull(dataItemDto.aceFlag())) {
      return "";
    }
    return "ACE: %s".formatted(BooleanUtil.yesNoFromBoolean(dataItemDto.aceFlag()));
  }

  String getDisplayCaseOfficer(ApplicationDataItemDto dataItemDto,
                               Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtosMap) {
    return Objects.nonNull(dataItemDto.caseOfficerWuaId())
        ? portalUserDtosMap.get(WebUserAccountId.from(dataItemDto.caseOfficerWuaId())).displayName()
        : "";
  }

  String getDisplayCamUser(ApplicationDataItemDto dataItemDto,
                           Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtosMap,
                           TeamType teamType) {
    return TeamType.REGULATOR.equals(teamType) && Objects.nonNull(dataItemDto.camWuaId())
        ? portalUserDtosMap.get(WebUserAccountId.from(dataItemDto.camWuaId())).displayName()
        : "";
  }

  String getDisplayTechnicalReviewer(ApplicationDataItemDto dataItemDto,
                                     Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtosMap,
                                     TeamType teamType) {
    return TeamType.REGULATOR.equals(teamType) && Objects.nonNull(dataItemDto.technicalReviewerWuaId())
        ? portalUserDtosMap.get(WebUserAccountId.from(dataItemDto.technicalReviewerWuaId())).displayName()
        : "";
  }

  String getSubmittedDateTime(ApplicationDataItemDto dataItemDto) {
    return dataItemDto.submittedDateTime() != null
        ? DateUtils.format(dataItemDto.submittedDateTime(), DateUtils.DATE_TIME)
        : "";
  }

  String getSubmittedByName(
      ApplicationDataItemDto dataItemDto,
      Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtoByWuaId
  ) {
    if (dataItemDto.submittedByWuaId() == null) {
      return "";
    }

    var matchingPortalUserDto = portalUserDtoByWuaId.get(WebUserAccountId.from(dataItemDto.submittedByWuaId()));
    return matchingPortalUserDto.displayName();
  }

  String getConsultationDeadline(ApplicationDataItemDto dataItemDto) {
    if (!Boolean.TRUE.equals(dataItemDto.consultationOpen())) {
      return "";
    }

    return DateUtils.format(dataItemDto.consultationDeadline(), DateUtils.DATE_TIME);
  }

  String getApplicationUpdateDeadline(ApplicationDataItemDto dataItemDto) {
    if (!Boolean.TRUE.equals(dataItemDto.applicationUpdateOpen())) {
      return "";
    }

    return DateUtils.format(dataItemDto.applicationUpdateDeadline(), DateUtils.DATE_TIME);
  }

  String getTechnicalReviewDeadline(ApplicationDataItemDto dataItemDto) {
    if (!Boolean.TRUE.equals(dataItemDto.technicalReviewOpen())) {
      return "";
    }

    return DateUtils.format(dataItemDto.technicalReviewDeadline(), DateUtils.DATE_TIME);
  }

  String getOperator(ApplicationDataItemDto dataItemDto, Map<Integer, String> organisationUnitNameById) {
    return organisationUnitNameById.getOrDefault(dataItemDto.operatorId(), "MISSING OPERATOR");
  }

  String getLicences(ApplicationDataItemDto dataItemDto) {
    return AssetType.FIELD.equals(dataItemDto.assetType())
        ? dataItemDto.licences()
        : "";
  }

  Boolean getConsentIssuedAndNotYetActive(ApplicationDataItemDto dataItemDto) {
    return dataItemDto.consentIssued()
        && dataItemDto.consentStartDate().isAfter(LocalDate.now(clock));
  }

  Boolean getConsentIssuedAndActive(ApplicationDataItemDto dataItemDto) {
    var today = LocalDate.now(clock);
    var consentStartDate = dataItemDto.consentStartDate();
    var consentEndDate = dataItemDto.consentEndDate();

    return dataItemDto.consentIssued()
        && DateUtils.isAfterOrEqualTo(today, consentStartDate)
        && DateUtils.isBeforeOrEqualTo(today, consentEndDate);
  }

  Boolean getConsentIssuedAndExpired(ApplicationDataItemDto dataItemDto) {
    return dataItemDto.consentIssued()
        && dataItemDto.consentEndDate().isBefore(LocalDate.now(clock));
  }

  public ApplicationDataItemView getApplicationDataItemView(
      ApplicationDataItemDto dataItemDto,
      ServiceUserDetail user,
      TeamType teamType,
      Map<Integer, String> organisationUnitNameById,
      Map<Integer, FieldJson> fieldJsonById,
      Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtoByWuaId
  ) {
    var withdrawalOpen = Boolean.TRUE.equals(dataItemDto.withdrawalOpen());
    var applicationUpdateOpen = Boolean.TRUE.equals(dataItemDto.applicationUpdateOpen());
    var furtherInformationOpen = FurtherInformationStatus.OPEN.equals(dataItemDto.consultationFurtherInformationStatus());
    var approvedForIssue = Boolean.TRUE.equals(dataItemDto.approvedForIssue());

    var userAction = getApplicationDataItemUserActionFromUser(user);

    var builder = ApplicationDataItemView.newBuilder()
        .withApplicationId(dataItemDto.applicationId())
        .withType(dataItemDto.type().getDisplayName())
        .withDuration(getDisplayConsentDuration(dataItemDto))
        .withReference(getDisplayReference(dataItemDto, userAction))
        .withOperator(getOperator(dataItemDto, organisationUnitNameById))
        .withAsset(dataItemDto.assetName())
        .withGeographicArea(getDisplayAssetLocation(dataItemDto, fieldJsonById))
        .withStatus(dataItemDto.status().getDisplayName())
        .withSubmittedDateTime(getSubmittedDateTime(dataItemDto))
        .withSubmittedBy(getSubmittedByName(dataItemDto, portalUserDtoByWuaId))
        .withAceFlag(getDisplayAceFlag(dataItemDto))
        .withCaseOfficer(getDisplayCaseOfficer(dataItemDto, portalUserDtoByWuaId))
        .withCamUser(getDisplayCamUser(dataItemDto, portalUserDtoByWuaId, teamType))
        .withWithdrawalOpen(withdrawalOpen)
        .withTechnicalReviewer(getDisplayTechnicalReviewer(dataItemDto, portalUserDtoByWuaId, teamType))
        .withTechnicalReviewOpen(dataItemDto.technicalReviewOpen())
        .withTechnicalReviewDeadline(getTechnicalReviewDeadline(dataItemDto))
        .withApplicationUpdateOpen(applicationUpdateOpen)
        .withApplicationUpdateDeadline(getApplicationUpdateDeadline(dataItemDto))
        .withConsultationOpen(dataItemDto.consultationOpen())
        .withConsultationDeadline(getConsultationDeadline(dataItemDto))
        .withConsultationFurtherInformationOpen(furtherInformationOpen)
        .withLicences(getLicences(dataItemDto))
        .withApprovedForIssue(approvedForIssue)
        .withConsentIssuedAndNotYetActive(getConsentIssuedAndNotYetActive(dataItemDto))
        .withConsentIssuedAndActive(getConsentIssuedAndActive(dataItemDto))
        .withConsentIssuedAndExpired(getConsentIssuedAndExpired(dataItemDto));

    removeTagsForTeamType(teamType, builder);

    return builder.build();
  }

  void removeTagsForTeamType(TeamType teamType, ApplicationDataItemView.Builder builder) {
    if (TeamType.INDUSTRY.equals(teamType)) {
      removeTechnicalReviewTag(builder);
      removeConsultationTag(builder);
      removeFurtherInformationTag(builder);
      removeApprovedForIssueTag(builder);
      return;
    }

    if (TeamType.OPRED.equals(teamType)) {
      removeTechnicalReviewTag(builder);
      removeApplicationUpdateTag(builder);
      removeWithdrawalTag(builder);
      removeApprovedForIssueTag(builder);
    }
  }

  private void removeWithdrawalTag(ApplicationDataItemView.Builder builder) {
    builder.withWithdrawalOpen(null);
  }

  private void removeApplicationUpdateTag(ApplicationDataItemView.Builder builder) {
    builder.withApplicationUpdateOpen(null).withApplicationUpdateDeadline(null);
  }

  private void removeFurtherInformationTag(ApplicationDataItemView.Builder builder) {
    builder.withConsultationFurtherInformationOpen(null);
  }

  private void removeTechnicalReviewTag(ApplicationDataItemView.Builder builder) {
    builder.withTechnicalReviewOpen(null).withTechnicalReviewDeadline(null);
  }

  private void removeConsultationTag(ApplicationDataItemView.Builder builder) {
    builder.withConsultationOpen(null).withConsultationDeadline(null);
  }

  private void removeApprovedForIssueTag(ApplicationDataItemView.Builder builder) {
    builder.withApprovedForIssue(null);
  }

  public List<ApplicationDataItemDto> runGetDataItemDtoQuery(List<Condition> conditions) {
    return applicationDataItemViewQueryService.runQueryWithCustom(conditions, selectQuery ->
            selectQuery.addOrderBy(greatest(
                APPLICATION_VERSIONS.SUBMITTED_DATE_TIME,
                APPLICATION_VERSIONS.CREATED_DATE_TIME).desc()),
        ApplicationDataItemDto.class);
  }
}
