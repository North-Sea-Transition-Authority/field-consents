package uk.co.nstauthority.fieldconsents.query;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.search.SearchFilterForm;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilter;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilterForm;

public class ApplicationDataFilterFormTestUtil {

  public static final int ORGANISATION_UNIT_ID = 1;

  public static final String ORGANISATION_NAME = "Organisation name";

  public static final int ORGANISATION_GROUP_ID = 1;

  public static final String ORGANISATION_GROUP_NAME = "Organisation group name";

  public static final String FIELD1_ASSET_ID_STRING = "1234";

  public static final String FIELD1_ASSET_KEY = FIELD1_ASSET_ID_STRING + AssetType.FIELD.name();

  public static final String FIELD2_ASSET_ID_STRING = "4321";

  public static final String FIELD2_ASSET_KEY = FIELD2_ASSET_ID_STRING + AssetType.FIELD.name();

  public static final RestSearchItem ORGANISATION_REST_SEARCH_ITEM = new RestSearchItem(String.valueOf(ORGANISATION_UNIT_ID), ORGANISATION_NAME);

  public static final RestSearchItem ORGANISATION_GROUP_REST_SEARCH_ITEM = new RestSearchItem(String.valueOf(ORGANISATION_GROUP_ID), ORGANISATION_GROUP_NAME);

  public static final RestSearchItem FIELD1_REST_SEARCH_ITEM = new RestSearchItem(FIELD1_ASSET_ID_STRING, AssetType.FIELD.name());

  public static final RestSearchItem FIELD2_REST_SEARCH_ITEM = new RestSearchItem(FIELD2_ASSET_ID_STRING, AssetType.FIELD.name());

  public static final String TERMINAL1_ASSET_ID_STRING = "5678";

  public static final String TERMINAL1_ASSET_KEY = TERMINAL1_ASSET_ID_STRING + AssetType.TERMINAL.name();

  public static final RestSearchItem TERMINAL1_REST_SEARCH_ITEM = new RestSearchItem(TERMINAL1_ASSET_ID_STRING, AssetType.TERMINAL.name());

  public static final String TERMINAL2_ASSET_ID_STRING = "8765";

  public static final String TERMINAL2_ASSET_KEY = TERMINAL2_ASSET_ID_STRING + AssetType.TERMINAL.name();

  public static final RestSearchItem TERMINAL2_REST_SEARCH_ITEM = new RestSearchItem(TERMINAL2_ASSET_ID_STRING, AssetType.TERMINAL.name());

  public static final Integer APPLICATION_NO = 10;

  public static WorkAreaFilter getDefaultFilter() {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    workAreaFilter.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return workAreaFilter;
  }

  public static WorkAreaFilter getFilterWithDurationTypes() {
    var workAreaFilter = new WorkAreaFilter();
    workAreaFilter.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    return workAreaFilter;
  }

  public static WorkAreaFilterForm getWorkAreaFormForDefaultFilter() {
    var workAreaForm = new WorkAreaFilterForm();
    workAreaForm.setStatuses(List.of(ApplicationVersionStatus.IN_PROGRESS, ApplicationVersionStatus.SUBMITTED));
    workAreaForm.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.FLARE, ApplicationType.VENT));
    return workAreaForm;
  }

  public static WorkAreaFilterForm getWorkAreaFormForFilterWithDurationTypes() {
    var workAreaForm = new WorkAreaFilterForm();
    workAreaForm.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    return workAreaForm;
  }

  public static SearchFilterForm getBasicSearchFilterForm() {
    var form = new SearchFilterForm();
    form.setReferenceNumber(String.valueOf(APPLICATION_NO));
    form.setStatuses(List.of(ApplicationVersionStatus.SUBMITTED, ApplicationVersionStatus.IN_PROGRESS));
    form.setApplicationTypes(List.of(ApplicationType.PRODUCTION, ApplicationType.VENT));
    form.setDurationTypes(List.of(ConsentLengthType.ANNUAL, ConsentLengthType.SHORT_TERM, ConsentLengthType.LONG_TERM));
    form.setOperatorId(ORGANISATION_UNIT_ID);
    return form;
  }
}
