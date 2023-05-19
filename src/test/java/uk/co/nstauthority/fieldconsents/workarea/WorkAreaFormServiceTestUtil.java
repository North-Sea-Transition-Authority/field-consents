package uk.co.nstauthority.fieldconsents.workarea;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

public class WorkAreaFormServiceTestUtil {

  public static final int ORGANISATION_UNIT_ID = 1;

  public static final String ORGANISATION_NAME = "Organisation name";

  public static final String FIELD_ASSET_ID_STRING = "1234";

  public static final String FIELD_ASSET_KEY = FIELD_ASSET_ID_STRING + AssetType.FIELD.name();

  public static final RestSearchItem ORGANISATION_REST_SEARCH_ITEM = new RestSearchItem(String.valueOf(ORGANISATION_UNIT_ID), ORGANISATION_NAME);

  public static final RestSearchItem FIELD_REST_SEARCH_ITEM = new RestSearchItem(FIELD_ASSET_ID_STRING, AssetType.FIELD.name());

  public static final String TERMINAL_ASSET_ID_STRING = "5678";

  public static final Integer TERMINAL_ASSET_ID_INTEGER = 5678;

  public static final String TERMINAL_ASSET_KEY = TERMINAL_ASSET_ID_STRING + AssetType.TERMINAL.name();

  public static final String APPLICATION_NO = "10";

  static WorkAreaFilter getDefaultFilter() {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    workAreaFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return workAreaFilter;
  }

  static WorkAreaFilter getFilterWithDurationTypes() {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    return workAreaFilter;
  }

  static WorkAreaForm getWorkAreaFormForDefaultFilter() {
    var workAreaForm = new WorkAreaForm();
    workAreaForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    workAreaForm.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return workAreaForm;
  }

  static WorkAreaForm getWorkAreaFormForFilterWithDurationTypes() {
    var workAreaForm = new WorkAreaForm();
    workAreaForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    return workAreaForm;
  }

  static WorkAreaForm getWorkAreaFormForFilterWithFieldAndOperator() {
    var workAreaForm = new WorkAreaForm();
    workAreaForm.setAssetKey("%s%s".formatted(TERMINAL_ASSET_ID_INTEGER, AssetType.FIELD.name()));
    workAreaForm.setOperatorId(ORGANISATION_UNIT_ID);
    return workAreaForm;
  }
}
