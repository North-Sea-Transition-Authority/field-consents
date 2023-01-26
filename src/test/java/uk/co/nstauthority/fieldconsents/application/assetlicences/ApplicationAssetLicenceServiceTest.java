package uk.co.nstauthority.fieldconsents.application.assetlicences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.allAssetsLicences;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1Licence1;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1Licence2;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset1Licences;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset2Licences;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset3;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.fieldAsset3Licences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithNullOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.licences.LicenceTestUtil.LICENCE_ID_3;
import static uk.co.nstauthority.fieldconsents.licences.LicenceTestUtil.LICENCE_REF_3;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationAssetLicenceServiceTest {

  @Mock
  private ApplicationAssetLicenceRepository applicationAssetLicenceRepository;

  @Captor
  private ArgumentCaptor<ApplicationAssetLicence> applicationAssetLicenceArgumentCaptor;

  private ApplicationAssetLicenceService applicationAssetLicenceService;

  private ApplicationAsset applicationAsset;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationAssetLicenceService = new ApplicationAssetLicenceService(applicationAssetLicenceRepository);
    applicationAsset = fieldAsset1;
    applicationVersion = applicationAsset.getApplicationVersion();
  }

  @Test
  void createAssetLicences_emptyLicences() {
    assertThatThrownBy(() ->
        applicationAssetLicenceService.createAssetLicences(applicationAsset, field1JsonWithNullOperatorAndLicences))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No licences found for asset %s with id %s."
            .formatted(field1JsonWithNullOperatorAndLicences.getName(), field1JsonWithNullOperatorAndLicences.getId()));
  }

  @Test
  void createAssetLicences_nullLicences() {
    FieldWithOperatorAndLicencesJson fieldJson =
        new FieldWithOperatorAndLicencesJson(FIELD_ID_1, FIELD_NAME_1, OrganisationUnitTestUtil.orgUnit1Json, null);

    assertThatThrownBy(() ->
        applicationAssetLicenceService.createAssetLicences(applicationAsset, fieldJson))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("No licences found for asset %s with id %s."
            .formatted(fieldJson.getName(), fieldJson.getId()));
  }

  @Test
  void createAssetLicences_licencesExist() {
    applicationAssetLicenceService.createAssetLicences(applicationAsset, field1JsonWithOperatorAndLicences);

    verify(applicationAssetLicenceRepository, times(3))
        .save(applicationAssetLicenceArgumentCaptor.capture());

    List<ApplicationAssetLicence> applicationAssetLicences = applicationAssetLicenceArgumentCaptor.getAllValues();

    assertThat(applicationAssetLicences).hasSize(3);
    for (int i = 0; i < applicationAssetLicences.size(); i++) {
      ApplicationAssetLicence applicationAssetLicence = applicationAssetLicences.get(i);
      assertThat(applicationAssetLicence)
          .extracting(
              ApplicationAssetLicence::getApplicationVersion,
              ApplicationAssetLicence::getApplicationAsset,
              ApplicationAssetLicence::getLicenceId,
              ApplicationAssetLicence::getCachedLicenceRef
          )
          .containsExactly(
              applicationAsset.getApplicationVersion(),
              applicationAsset,
              field1JsonWithOperatorAndLicences.getLicences().get(i).licenceId(),
              field1JsonWithOperatorAndLicences.getLicences().get(i).licenceRef()
          );
    }
  }

  @Test
  void getAssetLicences() {
    ApplicationAssetLicence fieldAsset1Licence3 = new ApplicationAssetLicence();
    fieldAsset1Licence3.setApplicationVersion(applicationAsset.getApplicationVersion());
    fieldAsset1Licence3.setApplicationAsset(applicationAsset);
    fieldAsset1Licence3.setLicenceId(LICENCE_ID_3);
    fieldAsset1Licence3.setCachedLicenceRef(LICENCE_REF_3);
    when(applicationAssetLicenceRepository.findAllByApplicationAssetOrderByCachedLicenceRefAsc(applicationAsset))
        .thenReturn(List.of(fieldAsset1Licence1, fieldAsset1Licence2, fieldAsset1Licence3));

    assertThat(applicationAssetLicenceService.getAssetLicences(applicationAsset))
        .containsExactly(fieldAsset1Licence1, fieldAsset1Licence2, fieldAsset1Licence3);
  }

  @Test
  void getAssetLicencesMap() {
    when(applicationAssetLicenceRepository.findAllByApplicationVersion(applicationVersion))
        .thenReturn(allAssetsLicences);

    assertThat(applicationAssetLicenceService.getAssetLicencesMap(applicationVersion))
        .containsOnly(
            entry(fieldAsset1, fieldAsset1Licences),
            entry(fieldAsset2, fieldAsset2Licences),
            entry(fieldAsset3, fieldAsset3Licences)
        );
  }

  @Test
  void deleteAssetLicences() {
    applicationAssetLicenceService.deleteAssetLicences(applicationAsset);

    verify(applicationAssetLicenceRepository, times(1))
        .deleteApplicationAssetLicencesByApplicationAsset(applicationAsset);
  }
}
