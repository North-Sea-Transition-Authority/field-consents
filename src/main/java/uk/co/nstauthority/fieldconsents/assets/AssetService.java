package uk.co.nstauthority.fieldconsents.assets;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService.TERMINAL_INACTIVE_VALIDATION_MESSAGE;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.CREATE_FCS_APPLICATIONS;

import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.assets.facility.FacilityService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.hubs.HubService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalStatus;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;
import uk.co.nstauthority.fieldconsents.teams.TeamService;

@Service
public class AssetService {

  private static final String FIELD_LOOKUP_PURPOSE = "Determining whether an application can be started for this field";
  private static final String TERMINAL_LOOKUP_PURPOSE = "Determining whether an application can be started for this terminal";

  private final FieldService fieldService;
  private final TerminalService terminalService;
  private final OrganisationUnitPermissionService organisationUnitPermissionService;
  private final TeamService teamService;
  private final FacilityService facilityService;
  private final HubService hubService;

  AssetService(
      FieldService fieldService,
      TerminalService terminalService,
      OrganisationUnitPermissionService organisationUnitPermissionService,
      TeamService teamService,
      FacilityService facilityService,
      HubService hubService
  ) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
    this.organisationUnitPermissionService = organisationUnitPermissionService;
    this.teamService = teamService;
    this.facilityService = facilityService;
    this.hubService = hubService;
  }

  @SuppressWarnings("unchecked")
  public Optional<AssetJson> findAsset(AssetKey assetKey) {
    if (Objects.isNull(assetKey)) {
      return Optional.empty();
    }

    var assetId = assetKey.assetId();
    var requestPurpose = "looking up asset for asset key";

    var assetJsonOptional = switch (assetKey.assetType()) {
      case FIELD -> fieldService.findField(assetId, requestPurpose);
      case TERMINAL -> terminalService.findTerminal(assetId, requestPurpose);
      case FACILITY -> facilityService.findFacilityWithOperator(assetId, requestPurpose);
      case HUB -> hubService.findHubWithOperator(assetId, requestPurpose);
    };

    return (Optional<AssetJson>) assetJsonOptional;
  }

  public AssetJson getAsset(AssetKey assetKey) {
    return findAsset(assetKey).orElseThrow(() -> new EntityNotFoundException("Asset with key %s not found".formatted(assetKey)));
  }

  public void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(AssetKey assetKey, ServiceUserDetail user) {
    var assetType = assetKey.assetType();
    var startApplicationDecision = switch (assetType) {
      case FIELD -> getStartApplicationDecisionForField(
          user,
          () -> fieldService.getFieldWithOperatorAndLicences(assetKey.assetId(), FIELD_LOOKUP_PURPOSE)
      );
      case TERMINAL -> getStartApplicationDecisionForTerminal(
          user,
          () -> terminalService.getTerminalWithOperator(assetKey.assetId(), TERMINAL_LOOKUP_PURPOSE)
      );
      default ->
          StartApplicationDecision.notAllowed(List.of("Cannot start application for %s".formatted(assetType.getDisplayName())));
    };

    if (!startApplicationDecision.canBeStarted()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot start application for %s [%d]"
          .formatted(assetKey.assetType().getDisplayName().toLowerCase(), assetKey.assetId()));
    }
  }

  public StartApplicationDecision getStartApplicationDecisionForField(
      ServiceUserDetail user,
      Supplier<FieldWithOperatorAndLicencesJson> fieldJsonSupplier
  ) {
    if (!teamService.isIndustryUser(user)) {
      return StartApplicationDecision.notAllowed(List.of());
    }

    var reasonsWhyApplicationCannotBeStarted = new ArrayList<String>();
    var fieldJson = fieldJsonSupplier.get();

    getOperatorPermissionCheckReason(fieldJson, user).ifPresent(reasonsWhyApplicationCannotBeStarted::add);

    if (!fieldJson.licencesExist()) {
      reasonsWhyApplicationCannotBeStarted.add("There are no licences associated to this field");
    }

    if (!FIELD_STATUSES_ALLOWED.contains(fieldJson.getStatusJson().status())) {
      reasonsWhyApplicationCannotBeStarted.add("The field %s".formatted(FIELD_STATUSES_ALLOWED_VALIDATION_MESSAGE));
    }

    return reasonsWhyApplicationCannotBeStarted.isEmpty()
        ? StartApplicationDecision.allowed()
        : StartApplicationDecision.notAllowed(reasonsWhyApplicationCannotBeStarted);
  }

  public StartApplicationDecision getStartApplicationDecisionForTerminal(
      ServiceUserDetail user,
      Supplier<TerminalWithOperatorJson> terminalJsonSupplier
  ) {
    if (!teamService.isIndustryUser(user)) {
      return StartApplicationDecision.notAllowed(List.of());
    }

    var reasonsWhyApplicationCannotBeStarted = new ArrayList<String>();
    var terminalJson = terminalJsonSupplier.get();

    getOperatorPermissionCheckReason(terminalJson, user).ifPresent(reasonsWhyApplicationCannotBeStarted::add);

    if (!TerminalStatus.ACTIVE.equals(terminalJson.getStatus())) {
      reasonsWhyApplicationCannotBeStarted.add(
          "%s %s".formatted(terminalJson.getName(), TERMINAL_INACTIVE_VALIDATION_MESSAGE));
    }

    return reasonsWhyApplicationCannotBeStarted.isEmpty()
        ? StartApplicationDecision.allowed()
        : StartApplicationDecision.notAllowed(reasonsWhyApplicationCannotBeStarted);
  }

  private Optional<String> getOperatorPermissionCheckReason(AssetWithOperatorJson assetWithOperatorJson, ServiceUserDetail user) {
    var assetTypeLowercase = assetWithOperatorJson.getAssetType().getDisplayName().toLowerCase();

    if (!assetWithOperatorJson.operatorExists()) {
      return Optional.of("An operator does not exist for this %s".formatted(assetTypeLowercase));
    }

    var operatorOuId = assetWithOperatorJson.getOperatorJson().organisationUnitId();
    if (!organisationUnitPermissionService.hasOperatorPermission(user, operatorOuId, CREATE_FCS_APPLICATIONS)) {
      return Optional.of("You are missing permissions to create applications for this %s".formatted(assetTypeLowercase));
    }

    return Optional.empty();
  }

}
