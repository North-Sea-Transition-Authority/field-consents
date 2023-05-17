package uk.co.nstauthority.fieldconsents.workarea;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationAssets.APPLICATION_ASSETS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;
import static uk.co.nstauthority.fieldconsents.workarea.WorkAreaFormServiceTestUtil.ORGANISATION_UNIT_ID;

import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.assets.AssetService;
import uk.co.nstauthority.fieldconsents.assets.AssetTestUtil;

@ExtendWith(MockitoExtension.class)
class WorkAreaFilterServiceTest {

  @Mock
  private AssetService assetService;

  private WorkAreaFilterService workAreaFilterService;

  private WorkAreaFilter filter;
  private WorkAreaForm form;

  @BeforeEach
  void setup() {
    workAreaFilterService = new WorkAreaFilterService(assetService);
    filter = new WorkAreaFilter();
    form = new WorkAreaForm();
  }

  @Test
  void getConditions_EmptyFilter_AssertEmpty() {
    var conditions = workAreaFilterService.getConditions(filter);
    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_StatusesSelected() {
    var status = ApplicationVersionStatus.IN_PROGRESS;
    form.setStatuses(Collections.singletonList(status));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.STATUS.in(Collections.singletonList(status.getEnumName()))
    );
  }

  @Test
  void getConditions_ApplicationTypesSelected() {
    var applicationType = ApplicationType.PRODUCTION;
    form.setApplicationTypes(Collections.singletonList(applicationType));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATIONS.TYPE.in(Collections.singletonList(applicationType.getEnumName()))
    );
  }

  @Test
  void getConditions_DurationTypesSelected() {
    var durationTypes = ConsentLengthType.LONG_TERM;
    form.setDurationTypes(Collections.singletonList(durationTypes));
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        CONSENT_LENGTHS.CONSENT_LENGTH.in(Collections.singletonList(durationTypes.getEnumName()))
    );
  }

  @Test
  void getConditions_AssetNotFound() {
    form.setAssetKey(WorkAreaFormServiceTestUtil.FIELD_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.empty());
    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).isEmpty();
  }

  @Test
  void getConditions_FieldSelected() {
    form.setAssetKey(WorkAreaFormServiceTestUtil.FIELD_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.of(AssetTestUtil.field1AssetJson));
    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATION_ASSETS.FIELD_ID.eq(FIELD_ID_1)
    );
  }

  @Test
  void getConditions_TerminalSelected() {
    form.setAssetKey(WorkAreaFormServiceTestUtil.TERMINAL_ASSET_KEY);
    filter.update(form);

    when(assetService.getAssetFromKey(filter.getAssetKey())).thenReturn(Optional.of(AssetTestUtil.terminal1AssetJson));
    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATION_ASSETS.TERMINAL_ID.eq(TERMINAL_ID_1)
    );
  }

  @Test
  void getConditions_OperatorSelected() {
    form.setOperatorId(ORGANISATION_UNIT_ID);
    filter.update(form);

    var conditions = workAreaFilterService.getConditions(filter);

    assertThat(conditions).containsExactly(
        APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(ORGANISATION_UNIT_ID)
    );
  }
}