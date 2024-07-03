package uk.co.nstauthority.fieldconsents.assets;

import static uk.co.nstauthority.fieldconsents.assets.fields.FieldService.FIELD_STATUSES_ALLOWED;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldJson;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalStatus;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitPermissionService;

@Service
public class AssetService {

  private static final String FIELD_LOOKUP_PURPOSE = "Determining whether an application can be started for this field";
  private static final String TERMINAL_LOOKUP_PURPOSE = "Determining whether an application can be started for this terminal";

  private final FieldService fieldService;
  private final TerminalService terminalService;

  AssetService(
      FieldService fieldService,
      TerminalService terminalService,
      OrganisationUnitPermissionService organisationUnitPermissionService
  ) {
    this.fieldService = fieldService;
    this.terminalService = terminalService;
  }

  @Deprecated
  public Optional<AssetJson> getAssetFromKey(String assetKey) {

    if (assetKey == null) {
      return Optional.empty();
    } else if (assetKey.endsWith(AssetType.FIELD.name())) {
      var fieldId = Integer.valueOf(assetKey.replace(AssetType.FIELD.name(), ""));
      FieldJson fieldJson = fieldService.findField(fieldId, "Field asset picked from search selector")
          .orElse(null);
      return Optional.ofNullable(fieldJson);
    } else if (assetKey.endsWith(AssetType.TERMINAL.name())) {
      var terminalId = Integer.valueOf(assetKey.replace(AssetType.TERMINAL.name(), ""));
      TerminalJson terminalJson = terminalService.findTerminal(terminalId, "Terminal asset picked from search selector")
          .orElse(null);
      return Optional.ofNullable(terminalJson);
    } else {
      throw new RuntimeException("Not a valid AssetKey: " + assetKey);
    }
  }

  @Deprecated
  public AssetJson getAsset(String assetKey) {
    return getAssetFromKey(assetKey)
        .orElseThrow(() -> new RuntimeException("Asset with key %s not found".formatted(assetKey)));
  }

  public Optional<? extends AssetJson> getAsset(AssetKey assetKey, String requestPurpose) {
    if (Objects.isNull(assetKey)) {
      return Optional.empty();
    }

    return switch (assetKey.assetType()) {
      case FIELD -> fieldService.findField(assetKey.assetId(), requestPurpose);
      case TERMINAL -> terminalService.findTerminal(assetKey.assetId(), requestPurpose);
    };
  }

  public void throwForbiddenStatusExceptionIfCannotStartApplicationForAsset(AssetKey assetKey) {
    var assetId = assetKey.assetId();

    var startApplicationDecision = switch (assetKey.assetType()) {
      case FIELD -> {
        var fieldJson = fieldService.getFieldWithOperatorAndLicences(assetId, FIELD_LOOKUP_PURPOSE);
        yield getStartApplicationDecision(fieldJson);
      }
      case TERMINAL -> {
        var terminalJson = terminalService.getTerminalWithOperator(assetId, TERMINAL_LOOKUP_PURPOSE);
        yield getStartApplicationDecision(terminalJson);
      }
    };

    var assetTypeLowercase = assetKey.assetType().getDisplayName().toLowerCase();

    if (!startApplicationDecision.canBeStarted()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot start application for %s [%d]"
          .formatted(assetTypeLowercase, assetId));
    }
  }

  public StartApplicationDecision getStartApplicationDecision(FieldWithOperatorAndLicencesJson fieldJson) {
    var reasonsWhyApplicationCannotBeStarted = new ArrayList<String>();

    if (!fieldJson.licencesExist()) {
      reasonsWhyApplicationCannotBeStarted.add("There are no licences associated to this field");
    }

    if (!FIELD_STATUSES_ALLOWED.contains(fieldJson.getStatusJson().status())) {
      reasonsWhyApplicationCannotBeStarted.add("The field is not in a valid 'producing' status");
    }

    return reasonsWhyApplicationCannotBeStarted.isEmpty()
        ? StartApplicationDecision.allowed()
        : StartApplicationDecision.notAllowed(reasonsWhyApplicationCannotBeStarted);
  }

  public StartApplicationDecision getStartApplicationDecision(TerminalWithOperatorJson terminalJson) {
    if (!TerminalStatus.ACTIVE.equals(terminalJson.getStatus())) {
      return StartApplicationDecision.notAllowed(List.of("This facility is inactive"));
    }

    return StartApplicationDecision.allowed();
  }
}
