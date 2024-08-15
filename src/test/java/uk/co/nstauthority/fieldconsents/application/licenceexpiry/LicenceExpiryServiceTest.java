package uk.co.nstauthority.fieldconsents.application.licenceexpiry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldService;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson;
import uk.co.nstauthority.fieldconsents.licences.LicenceJson;
import uk.co.nstauthority.fieldconsents.licences.LicenceView;

@ExtendWith(MockitoExtension.class)
class LicenceExpiryServiceTest {

  private static final String REQUEST_PURPOSE = "checking the asset licences for expiry date";
  private static final ApplicationVersion APPLICATION_VERSION =
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private FieldService fieldService;

  @InjectMocks
  private LicenceExpiryService licenceExpiryService;

  @Test
  void getLicencesExpiringDuringConsentPeriod_withNoConsentDuration() {
    when(consentLengthService.findConsentLengthDetails(APPLICATION_VERSION))
        .thenReturn(Optional.empty());
    verify(consentLengthService, never())
        .getProposedConsentEndDate(any());
    verify(applicationAssetService, never())
        .findAssetsByApplicationVersionAndAssetRoles(any(), any());
    verify(fieldService, never())
        .findFieldsWithOperatorAndLicences(any(), any());
    assertThat(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(APPLICATION_VERSION))
        .isEmpty();
  }

  @Test
  void getLicencesExpiringDuringConsentPeriod_withConsentDuration_returnUniqueInAscendingOrderByDate() {
    var consentLengthDetails = new ConsentLengthDetails();
    var proposedConsentEndDate = LocalDate.of(2024, 8, 6);

    var applicationAssetWithEarlyLicence = new ApplicationAsset();
    applicationAssetWithEarlyLicence.setAssetType(AssetType.FIELD);
    var earlyFieldId = 200;
    applicationAssetWithEarlyLicence.setAssetId(earlyFieldId);

    var applicationAssetWithLateLicence = new ApplicationAsset();
    applicationAssetWithLateLicence.setAssetType(AssetType.FIELD);
    var lateFieldId = 401;
    applicationAssetWithLateLicence.setAssetId(lateFieldId);

    var applicationAssets = List.of(
        applicationAssetWithEarlyLicence,
        applicationAssetWithEarlyLicence,
        applicationAssetWithLateLicence
    );

    var earlyLicence1 = new LicenceJson(
        600,
        "P",
        123,
        "P123",
        LocalDate.of(2024, 8, 1)
    );

    var earlyLicence2 = new LicenceJson(
        603,
        "P",
        1000,
        "P1100",
        LocalDate.of(2024, 8, 1)
    );

    var earlyLicence3 = new LicenceJson(
        604,
        "PED",
        100,
        "PED100",
        LocalDate.of(2024, 8, 1)
    );

    var earlyLicence4 = new LicenceJson(
        605,
        "PED",
        1100,
        "PED1100",
        LocalDate.of(2024, 8, 1)
    );

    var earlyLicence5 = new LicenceJson(
        606,
        "P",
        127,
        "P127",
        LocalDate.of(2024, 8, 3)
    );

    var fieldWithOperatorAndEarlyLicencesJson = new FieldWithOperatorAndLicencesJson(
        earlyFieldId,
        "earlyTestField",
        null,
        null,
        null,
        null,
        List.of(
            earlyLicence5,
            earlyLicence4,
            earlyLicence3,
            earlyLicence2,
            earlyLicence1
        )
    );

    var fieldWithOperatorAndSimilarLicenceJson = new FieldWithOperatorAndLicencesJson(
        earlyFieldId,
        "earlyTestField",
        null,
        null,
        null,
        null,
        List.of(
            earlyLicence5,
            earlyLicence4,
            earlyLicence1
        )
    );

    var sameDayLicence = new LicenceJson(
        601,
        "P",
        456,
        "P456",
        LocalDate.of(2024, 8, 6)
    );

    var lateLicence = new LicenceJson(
        602,
        "PED",
        890,
        "PED890",
        LocalDate.of(2024, 8, 12)
    );
    var fieldWithOperatorAndLateLicenceJson = new FieldWithOperatorAndLicencesJson(
        lateFieldId,
        "lateTestField",
        null,
        null,
        null,
        null,
        List.of(sameDayLicence, lateLicence)
    );

    var fieldsWithOperatorAndLicenceJsons = List.of(
        fieldWithOperatorAndEarlyLicencesJson,
        fieldWithOperatorAndSimilarLicenceJson,
        fieldWithOperatorAndLateLicenceJson
    );

    var earlyLicenceView1 = LicenceView.from(earlyLicence1);
    var earlyLicenceView2 = LicenceView.from(earlyLicence2);
    var earlyLicenceView3 = LicenceView.from(earlyLicence3);
    var earlyLicenceView4 = LicenceView.from(earlyLicence4);
    var earlyLicenceView5 = LicenceView.from(earlyLicence5);

    when(consentLengthService.findConsentLengthDetails(APPLICATION_VERSION))
        .thenReturn(Optional.of(consentLengthDetails));
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails))
        .thenReturn(proposedConsentEndDate);
    when(applicationAssetService.findAssetsByApplicationVersionAndAssetRoles(
        APPLICATION_VERSION,
        List.of(AssetRole.PRIMARY, AssetRole.SECONDARY)))
        .thenReturn(applicationAssets);
    when(fieldService.findFieldsWithOperatorAndLicences(
        List.of(earlyFieldId, lateFieldId), REQUEST_PURPOSE))
        .thenReturn(fieldsWithOperatorAndLicenceJsons);

    assertThat(licenceExpiryService.getLicencesExpiringDuringConsentPeriod(APPLICATION_VERSION))
        .containsExactly(
            earlyLicenceView1,
            earlyLicenceView2,
            earlyLicenceView3,
            earlyLicenceView4,
            earlyLicenceView5
        );
  }
}
