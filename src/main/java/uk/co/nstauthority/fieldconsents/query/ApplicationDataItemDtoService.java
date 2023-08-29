package uk.co.nstauthority.fieldconsents.query;

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
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

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

  ApplicationDataItemDtoService(FieldService fieldService,
                                EnergyPortalUserService energyPortalUserService,
                                OrganisationUnitService organisationUnitService,
                                ApplicationService applicationService,
                                ApplicationVersionService applicationVersionService) {
    this.fieldService = fieldService;
    this.energyPortalUserService = energyPortalUserService;
    this.organisationUnitService = organisationUnitService;
    this.applicationService = applicationService;
    this.applicationVersionService = applicationVersionService;
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

  public Map<Long, EnergyPortalUserDto> getEnergyPortalUserDtoMapFromApplicationDataItemDtos(
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

    return energyPortalUserService.findByWuaIds(wuaIds)
        .stream()
        .collect(Collectors.toMap(EnergyPortalUserDto::webUserAccountId, Function.identity()));
  }

  public String getDisplayReference(ApplicationDataItemDto dataItemDto) {
    if (dataItemDto.getStatus().equals(ApplicationVersionStatus.IN_PROGRESS)
        && dataItemDto.getVersionNo() == 1) {
      return "Resume application";
    }

    if (dataItemDto.getStatus().equals(ApplicationVersionStatus.IN_PROGRESS)
        && dataItemDto.getVersionNo() > 1) {
      return "Resume %s".formatted(
          applicationService.generateApplicationReference(
              applicationVersionService.getApplicationVersionById(dataItemDto.getApplicationVersionId())));
    }

    return applicationService.generateApplicationReference(
        applicationVersionService.getApplicationVersionById(dataItemDto.getApplicationVersionId()));
  }

  public String getDisplayConsentDuration(ApplicationDataItemDto dataItemDto) {
    String info;
    var consentDuration = dataItemDto.getDuration();

    if (consentDuration == null) {
      return "";
    }

    switch (consentDuration) {
      case ANNUAL -> info = "%s %d".formatted(consentDuration.getShortDisplayName(), dataItemDto.getConsentYear());
      case LONG_TERM ->
          info = "%s %d - %d".formatted(
              consentDuration.getShortDisplayName(),
              dataItemDto.getLongTermStartYear(),
              dataItemDto.getLongTermEndYear()
          );
      case SHORT_TERM ->
          info = "%s %s - %s".formatted(
              consentDuration.getShortDisplayName(),
              DateUtils.format(dataItemDto.getShortTermStartDate(), DateUtils.SHORT_DATE),
              DateUtils.format(dataItemDto.getShortTermEndDate(), DateUtils.SHORT_DATE)
          );
      default -> info = "";
    }

    return info;
  }

  public String getDisplayAssetLocation(ApplicationDataItemDto dataItemDto, Map<Integer, FieldJson> fieldJsonsMap) {
    if (dataItemDto.getFieldId() == null) {
      return "";
    }

    var matchingFieldJson = fieldJsonsMap.get(dataItemDto.getFieldId());
    return matchingFieldJson != null
        ? matchingFieldJson.getGeographicArea().getDisplayName()
        : "Unknown area";
  }

  public String getDisplaySubmitter(ApplicationDataItemDto dataItemDto, Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    var matchingPortalUserDto = portalUserDtosMap.get(dataItemDto.getSubmittedByWuaId());
    return "Submitter: %s".formatted(matchingPortalUserDto.displayName());
  }

  public String getDisplayAceFlag(ApplicationDataItemDto dataItemDto) {
    return Boolean.TRUE.equals(dataItemDto.getAceFlag()) ? "ACE" : "";
  }

  public String getDisplayCaseOfficer(ApplicationDataItemDto dataItemDto, Map<Long, EnergyPortalUserDto> portalUserDtosMap) {
    return Objects.nonNull(dataItemDto.getCaseOfficerWuaId())
        ? "Case officer: %s".formatted(portalUserDtosMap.get(dataItemDto.getCaseOfficerWuaId()).displayName())
        : "";
  }

  public String getDisplayTechnicalReviewer(ApplicationDataItemDto dataItemDto,
                                            Map<Long, EnergyPortalUserDto> portalUserDtosMap,
                                            TeamType teamType) {
    return TeamType.REGULATOR.equals(teamType) && Objects.nonNull(dataItemDto.getTechnicalReviewerWuaId())
        ? "Technical reviewer: %s".formatted(portalUserDtosMap.get(dataItemDto.getTechnicalReviewerWuaId()).displayName())
        : "";
  }
}
