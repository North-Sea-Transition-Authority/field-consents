package uk.co.nstauthority.fieldconsents.application.summary.shared;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.licence.licence.LicenceApi;
import uk.co.fivium.energyportalapi.generated.client.LicencesProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.Licence;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicenceService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableRow;
import uk.co.nstauthority.fieldconsents.summary.SummaryTableView;

@ExtendWith(MockitoExtension.class)
class LicenceDetailsSummaryCardServiceTest {

  @Mock
  private ApplicationAssetLicenceService applicationAssetLicenceService;

  @Mock
  private LicenceApi licenceApi;

  @InjectMocks
  private LicenceDetailsSummaryCardService licenceDetailsSummaryCardService;

  @Captor
  private ArgumentCaptor<LicencesProjectionRoot> licencesProjectionRootCaptor;

  private final ApplicationVersion applicationVersion = new ApplicationVersion();
  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  @BeforeEach
  void setUp() {
    applicationVersion.setApplication(new Application(1));
  }

  @Test
  void getSummaryCard_noAssetLicences() {
    when(applicationAssetLicenceService.getAssetLicences(applicationVersion)).thenReturn(List.of());
    assertThat(licenceDetailsSummaryCardService.getSummaryCard(applicationVersion)).isEmpty();
  }

  @Test
  void getSummaryCard_noPrimaryAsset() {
    var applicationAssetLicence = new ApplicationAssetLicence();
    applicationAssetLicence.setApplicationAsset(ApplicationAssetTestUtil.newBuilder().build());

    when(applicationAssetLicenceService.getAssetLicences(applicationVersion)).thenReturn(List.of(applicationAssetLicence));

    assertThatThrownBy(() -> licenceDetailsSummaryCardService.getSummaryCard(applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("primary asset not found for application 1");
  }

  @ParameterizedTest
  @EnumSource(value = AssetType.class, names = "FIELD", mode = EXCLUDE)
  void getSummaryCard_primaryAssetNotField(AssetType assetType) {
    var applicationAssetLicence = new ApplicationAssetLicence();
    applicationAssetLicence.setApplicationAsset(
        ApplicationAssetTestUtil.newBuilder()
            .withAssetRole(AssetRole.PRIMARY)
            .withAssetType(assetType)
            .build()
    );

    when(applicationAssetLicenceService.getAssetLicences(applicationVersion)).thenReturn(List.of(applicationAssetLicence));

    assertThat(licenceDetailsSummaryCardService.getSummaryCard(applicationVersion)).isEmpty();
  }

  @Test
  void getSummaryCard() {
    var applicationAssetLicence1 = new ApplicationAssetLicence();
    applicationAssetLicence1.setLicenceId(1);
    applicationAssetLicence1.setApplicationAsset(
        ApplicationAssetTestUtil.newBuilder()
            .withCachedAssetName("Field A")
            .withAssetRole(AssetRole.PRIMARY)
            .withAssetType(AssetType.FIELD)
            .build()
    );

    var applicationAssetLicence2 = new ApplicationAssetLicence();
    applicationAssetLicence2.setLicenceId(2);
    applicationAssetLicence2.setApplicationAsset(
        ApplicationAssetTestUtil.newBuilder()
            .withCachedAssetName("Field B")
            .withAssetRole(AssetRole.SECONDARY)
            .withAssetType(AssetType.FIELD)
            .build()
    );

    var licenceIds = List.of(1, 2);
    var licencesQuery = new LicencesProjectionRoot().id().licenceRef().scheduleExpiryDate();
    var requestPurpose = new RequestPurpose("Looking up licence details for application summary");

    when(applicationAssetLicenceService.getAssetLicences(applicationVersion))
        .thenReturn(List.of(
            applicationAssetLicence1,
            applicationAssetLicence1,
            applicationAssetLicence2
        ));

    when(licenceApi.searchLicencesById(eq(licenceIds), any(), eq(requestPurpose)))
        .thenReturn(List.of(
            Licence.newBuilder().id(2).licenceRef("P2").build(),
            Licence.newBuilder().id(1).licenceRef("P1").scheduleExpiryDate(LocalDate.now(clock)).build()
        ));

    assertThat(licenceDetailsSummaryCardService.getSummaryCard(applicationVersion))
        .contains(SummaryCard.tableSummaryCard(new SummaryTableView(List.of(
            new SummaryTableRow(List.of("Licence reference", "Field name(s)", "Scheduled end date")),
            new SummaryTableRow(List.of("P1", "Field A", DateUtils.format(clock.instant(), DateUtils.LONG_DATE))),
            new SummaryTableRow(List.of("P2", "Field B", "None"))
        ))));

    verify(licenceApi).searchLicencesById(eq(licenceIds), licencesProjectionRootCaptor.capture(), eq(requestPurpose));
    assertThat(licencesProjectionRootCaptor.getValue().getFields()).containsAllEntriesOf(licencesQuery.getFields());
  }
}