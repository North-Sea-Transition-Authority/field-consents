package uk.co.nstauthority.fieldconsents.query;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus;
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

  ApplicationDataItemDtoService(FieldService fieldService,
                                EnergyPortalUserService energyPortalUserService,
                                OrganisationUnitService organisationUnitService,
                                ApplicationService applicationService,
                                ApplicationVersionService applicationVersionService,
                                PermissionService permissionService) {
    this.fieldService = fieldService;
    this.energyPortalUserService = energyPortalUserService;
    this.organisationUnitService = organisationUnitService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
    this.permissionService = permissionService;
  }

  public List<OrganisationUnitJson> getOrganisationUnitJsonsFromApplicationDataItemDtos(
      List<? extends ApplicationDataItemDto> applicationDataItemDtos) {
    return organisationUnitService.getOrganisationUnitsByIds(
        applicationDataItemDtos
            .stream()
            .map(ApplicationDataItemDto::getOperatorId)
            .toList(),
        ALL_ORG_UNITS_DATA_ITEM_PURPOSE);
  }

  public Map<Integer, FieldJson> getFieldJsonMapFromApplicationDataItemDtos(
      List<? extends ApplicationDataItemDto> applicationDataItemDtos) {
    var fieldJsons = fieldService.findFieldsByIds(applicationDataItemDtos
        .stream()
        .map(ApplicationDataItemDto::getFieldId)
        .distinct()
        .toList(), FIELD_LOOKUP_PURPOSE);

    return fieldJsons
        .stream()
        .collect(Collectors.toMap(
            FieldJson::getId,
            Function.identity())
        );
  }

  public Map<WebUserAccountId, EnergyPortalUserDto> getEnergyPortalUserDtoMapFromApplicationDataItemDtos(
      List<? extends ApplicationDataItemDto> applicationDataItemDtos) {

    var wuaIds = applicationDataItemDtos
        .stream()
        .flatMap(dataItem -> Stream.of(
                dataItem.getSubmittedByWuaId(),
                dataItem.getCaseOfficerWuaId(),
                dataItem.getTechnicalReviewerWuaId()
            ))
        .filter(Objects::nonNull)
        .map(WebUserAccountId::new)
        .distinct()
        .toList();

    return energyPortalUserService.getEnergyPortalUserMap(wuaIds);
  }

  public String getDisplayReference(ApplicationDataItemDto dataItemDto, ApplicationDataItemUserAction userAction) {
    var applicationVersion = applicationVersionService.getApplicationVersionById(dataItemDto.getApplicationVersionId());

    if (dataItemDto.getStatus() == ApplicationVersionStatus.IN_PROGRESS) {
      if (dataItemDto.getApplicationNo() == null) {
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

  public String getDisplayConsentDuration(ApplicationDataItemDto dataItemDto) {
    var consentDuration = dataItemDto.getDuration();

    if (Objects.isNull(consentDuration)) {
      return "";
    }

    return switch (consentDuration) {
      case ANNUAL -> "%s %d".formatted(consentDuration.getShortDisplayName(), dataItemDto.getConsentYear());
      case LONG_TERM -> "%s %d - %d".formatted(
          consentDuration.getShortDisplayName(),
          dataItemDto.getLongTermStartYear(),
          dataItemDto.getLongTermEndYear()
      );
      case SHORT_TERM -> "%s %s - %s".formatted(
          consentDuration.getShortDisplayName(),
          DateUtils.format(dataItemDto.getShortTermStartDate(), DateUtils.SHORT_DATE),
          DateUtils.format(dataItemDto.getShortTermEndDate(), DateUtils.SHORT_DATE)
      );
    };
  }

  public String getDisplayAssetLocation(ApplicationDataItemDto dataItemDto, Map<Integer, FieldJson> fieldJsonsMap) {
    if (Objects.isNull(dataItemDto.getFieldId())) {
      return "";
    }

    var matchingFieldJson = fieldJsonsMap.get(dataItemDto.getFieldId());
    return matchingFieldJson != null
        ? matchingFieldJson.getGeographicArea().getDisplayName()
        : "Unknown area";
  }

  public String getDisplayAceFlag(ApplicationDataItemDto dataItemDto) {
    return Boolean.TRUE.equals(dataItemDto.getAceFlag()) ? "ACE" : "";
  }

  public String getDisplayCaseOfficer(ApplicationDataItemDto dataItemDto,
                                      Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtosMap) {
    return Objects.nonNull(dataItemDto.getCaseOfficerWuaId())
        ? "Case officer: %s".formatted(
            portalUserDtosMap.get(WebUserAccountId.from(dataItemDto.getCaseOfficerWuaId())).displayName())
        : "";
  }

  public String getDisplayTechnicalReviewer(ApplicationDataItemDto dataItemDto,
                                            Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtosMap,
                                            TeamType teamType) {
    return TeamType.REGULATOR.equals(teamType) && Objects.nonNull(dataItemDto.getTechnicalReviewerWuaId())
        ? "Technical reviewer: %s".formatted(
            portalUserDtosMap.get(WebUserAccountId.from(dataItemDto.getTechnicalReviewerWuaId())).displayName())
        : "";
  }

  public String getSubmittedDateTime(ApplicationDataItemDto dataItemDto) {
    return ApplicationVersionStatus.SUBMITTED.equals(dataItemDto.getStatus())
        ? "Submitted: %s".formatted(DateUtils.format(dataItemDto.getSubmittedDateTime(), DateUtils.DATE_TIME))
        : "";
  }

  public String getSubmittedByName(
      ApplicationDataItemDto dataItemDto,
      Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtoByWuaId
  ) {
    if (!ApplicationVersionStatus.SUBMITTED.equals(dataItemDto.getStatus())) {
      return "";
    }

    var matchingPortalUserDto = portalUserDtoByWuaId.get(WebUserAccountId.from(dataItemDto.getSubmittedByWuaId()));
    return "Submitter: %s".formatted(matchingPortalUserDto.displayName());
  }

  public String getConsultationDeadline(ApplicationDataItemDto dataItemDto) {
    if (!Boolean.TRUE.equals(dataItemDto.getConsultationOpen())) {
      return "";
    }

    return DateUtils.format(dataItemDto.getConsultationDeadline(), DateUtils.DATE_TIME);
  }

  public String getApplicationUpdateDeadline(ApplicationDataItemDto dataItemDto) {
    if (!Boolean.TRUE.equals(dataItemDto.getApplicationUpdateOpen())) {
      return "";
    }

    return DateUtils.format(dataItemDto.getApplicationUpdateDeadline(), DateUtils.DATE_TIME);
  }

  public String getOperator(ApplicationDataItemDto dataItemDto, Map<Integer, String> organisationUnitNameById) {
    return organisationUnitNameById.getOrDefault(dataItemDto.getOperatorId(), "MISSING OPERATOR");
  }

  public String getAsset(ApplicationDataItemDto dataItemDto) {
    if (Objects.isNull(dataItemDto.getFieldId())) {
      return dataItemDto.getTerminalName();
    }

    return dataItemDto.getFieldName();
  }

  public ApplicationDataItem getApplicationDataItem(
      ApplicationDataItemDto dataItemDto,
      ServiceUserDetail user,
      TeamType teamType,
      Map<Integer, String> organisationUnitNameById,
      Map<Integer, FieldJson> fieldJsonById,
      Map<WebUserAccountId, EnergyPortalUserDto> portalUserDtoByWuaId
  ) {
    var withdrawalOpen = Boolean.TRUE.equals(dataItemDto.getWithdrawalOpen());
    var applicationUpdateOpen = Boolean.TRUE.equals(dataItemDto.getApplicationUpdateOpen());
    var furtherInformationOpen = FurtherInformationStatus.OPEN.equals(dataItemDto.getConsultationFurtherInformationStatus());

    var userAction = getApplicationDataItemUserActionFromUser(user);

    var builder = ApplicationDataItem.newBuilder()
        .withApplicationId(dataItemDto.getApplicationId())
        .withType(dataItemDto.getType().getDisplayName())
        .withDuration(getDisplayConsentDuration(dataItemDto))
        .withReference(getDisplayReference(dataItemDto, userAction))
        .withOperator(getOperator(dataItemDto, organisationUnitNameById))
        .withAsset(getAsset(dataItemDto))
        .withGeographicArea(getDisplayAssetLocation(dataItemDto, fieldJsonById))
        .withStatus(dataItemDto.getStatus().getDisplayName())
        .withSubmittedDateTime(getSubmittedDateTime(dataItemDto))
        .withSubmittedBy(getSubmittedByName(dataItemDto, portalUserDtoByWuaId))
        .withAceFlag(getDisplayAceFlag(dataItemDto))
        .withCaseOfficer(getDisplayCaseOfficer(dataItemDto, portalUserDtoByWuaId))
        .withWithdrawalOpen(withdrawalOpen)
        .withTechnicalReviewer(getDisplayTechnicalReviewer(dataItemDto, portalUserDtoByWuaId, teamType))
        .withApplicationUpdateOpen(applicationUpdateOpen)
        .withApplicationUpdateDeadline(getApplicationUpdateDeadline(dataItemDto))
        .withConsultationOpen(dataItemDto.getConsultationOpen())
        .withConsultationDeadline(getConsultationDeadline(dataItemDto))
        .withConsultationFurtherInformationOpen(furtherInformationOpen);

    removeTagsForTeamType(teamType, builder);

    return builder.build();
  }

  void removeTagsForTeamType(TeamType teamType, ApplicationDataItem.Builder builder) {
    if (TeamType.INDUSTRY.equals(teamType)) {
      removeConsultationTag(builder);
      removeFurtherInformationTag(builder);
      return;
    }

    if (TeamType.OPRED.equals(teamType)) {
      removeApplicationUpdateTag(builder);
      removeWithdrawalTag(builder);
    }
  }

  private void removeWithdrawalTag(ApplicationDataItem.Builder builder) {
    builder.withWithdrawalOpen(null);
  }

  private void removeApplicationUpdateTag(ApplicationDataItem.Builder builder) {
    builder.withApplicationUpdateOpen(null).withApplicationUpdateDeadline(null);
  }

  private void removeFurtherInformationTag(ApplicationDataItem.Builder builder) {
    builder.withConsultationFurtherInformationOpen(null);
  }

  private void removeConsultationTag(ApplicationDataItem.Builder builder) {
    builder.withConsultationOpen(null).withConsultationDeadline(null);
  }

}
