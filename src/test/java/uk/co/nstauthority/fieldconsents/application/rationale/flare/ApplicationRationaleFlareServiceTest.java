package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType.INCREASE;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleRepository;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalJson;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalStatus;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleFlareServiceTest {

  @Mock
  private ApplicationRationaleRepository repository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Spy
  @InjectMocks
  private ApplicationRationaleFlareService applicationRationaleFlareService;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationaleFlare;

  @Captor
  private ArgumentCaptor<ApplicationRationale> applicationRationaleFlareCaptor;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    applicationRationaleFlare = new ApplicationRationale();
    applicationRationaleFlare.setApplicationVersion(applicationVersion);
    applicationRationaleFlare.setId(1);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void doesApplicationRationaleExistFor(boolean exists) {
    when(repository.existsApplicationRationaleByApplicationVersion(applicationVersion)).thenReturn(exists);
    assertThat(applicationRationaleFlareService.doesApplicationRationaleExistFor(applicationVersion)).isEqualTo(exists);
  }

  @Test
  void findByApplicationVersion() {
    when(repository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationaleFlare));
    assertThat(applicationRationaleFlareService.findByApplicationVersion(applicationVersion))
        .isPresent()
        .get()
        .isEqualTo(applicationRationaleFlare);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = "FLARE", mode = EXCLUDE)
  void findByApplicationVersion_nonFlareApplication(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);
    assertThatThrownBy(() -> applicationRationaleFlareService.findByApplicationVersion(applicationVersion))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage(
            "Expected ApplicationVersion.Application type to be [FLARE] but was [%s]".formatted(applicationType));
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = "INCREASE", mode = EXCLUDE)
  void saveApplicationRationale_notIncrease_withComment(ApplicationRationaleType rationaleType) {
    var comment = "some comment";
    var flaringLocationAssetKeys = List.of("assetKey1", "assetKey2");
    var hostLocationAssetKey = "assetKey1";

    assertThatThrownBy(() -> applicationRationaleFlareService.saveApplicationRationale(
        applicationVersion,
        rationaleType,
        comment,
        flaringLocationAssetKeys,
        hostLocationAssetKey
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Comment is not applicable for ApplicationRationaleType.%s".formatted(rationaleType));
  }

  @ParameterizedTest
  @EnumSource(ApplicationRationaleType.class)
  void saveApplicationRationale_nonIncrease(ApplicationRationaleType rationaleType) {
    var flaringLocationAssetKeys = List.of("assetKey1", "assetKey2");
    var hostLocationAssetKey = "assetKey1";

    applicationRationaleFlareService.saveApplicationRationale(
        applicationVersion,
        rationaleType,
        null,
        flaringLocationAssetKeys,
        hostLocationAssetKey
    );

    verify(repository).save(applicationRationaleFlareCaptor.capture());
    assertThat(applicationRationaleFlareCaptor.getValue())
        .extracting(
            ApplicationRationale::getApplicationVersion,
            ApplicationRationale::getRationaleType,
            ApplicationRationale::getComment
        ).containsExactly(
            applicationVersion,
            rationaleType,
            null
        );

    verify(applicationAssetService).deleteAssetsByApplicationVersionAndAssetRoles(
        applicationVersion,
        Set.of(AssetRole.HOST, AssetRole.LOCATION)
    );

    for (var key : flaringLocationAssetKeys) {
      verify(applicationAssetService).createAssetForApplicationVersion(
          applicationVersion,
          key,
          AssetRole.LOCATION
      );
    }

    verify(applicationAssetService).createAssetForApplicationVersion(
        applicationVersion,
        hostLocationAssetKey,
        AssetRole.HOST
    );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = "INCREASE", mode = EXCLUDE)
  void getApplicationRationaleFlareSummaryCard_nonIncrease(ApplicationRationaleType applicationRationaleType) {
    applicationRationaleFlare.setRationaleType(applicationRationaleType);

    when(repository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationaleFlare));

    var flaringLocationViews = List.of(
        new ApplicationAssetView("assetKey1", "asset key 1", true),
        new ApplicationAssetView("assetKey2", "asset key 2", true),
        new ApplicationAssetView("assetKey3", "asset key 3", true)
    );
    doReturn(flaringLocationViews)
        .when(applicationRationaleFlareService)
        .getFlaringLocations(applicationVersion);

    when(applicationAssetService.findAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(new TerminalJson(1, "terminal", TerminalStatus.ACTIVE)));

    assertThat(applicationRationaleFlareService.getApplicationRationaleFlareSummaryCard(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue("Is this application for an increase or decrease?",
                    applicationRationaleType.getDisplayName()),
                new SummaryKeyValue("Where does the flaring take place?", "asset key 1, asset key 2, asset key 3"),
                new SummaryKeyValue("What is the host?", "terminal")
            ))
        ));
  }

  @Test
  void getApplicationRationaleFlareSummaryCard_increase() {
    var comment = "comment";
    applicationRationaleFlare.setComment(comment);
    applicationRationaleFlare.setRationaleType(INCREASE);

    when(repository.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationaleFlare));

    var flaringLocationViews = List.of(
        new ApplicationAssetView("assetKey1", "asset key 1", true),
        new ApplicationAssetView("assetKey2", "asset key 2", true),
        new ApplicationAssetView("assetKey3", "asset key 3", true)
    );
    doReturn(flaringLocationViews)
        .when(applicationRationaleFlareService)
        .getFlaringLocations(applicationVersion);

    when(applicationAssetService.findAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(new TerminalJson(1, "terminal", TerminalStatus.ACTIVE)));

    assertThat(applicationRationaleFlareService.getApplicationRationaleFlareSummaryCard(applicationVersion))
        .usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            new SummaryDataView(List.of(
                new SummaryKeyValue("Is this application for an increase or decrease?", INCREASE.getDisplayName()),
                new SummaryKeyValue("Why are you asking for an increase?", comment),
                new SummaryKeyValue("Where does the flaring take place?", "asset key 1, asset key 2, asset key 3"),
                new SummaryKeyValue("What is the host?", "terminal")
            ))
        ));
  }

  @Test
  void getFlaringLocations() {
    var terminalAsset = new TerminalJson(1, "terminal", TerminalStatus.ACTIVE);

    when(applicationAssetService.findAssetJsonListFor(applicationVersion, AssetRole.LOCATION))
        .thenReturn(Collections.singletonList(terminalAsset));

    assertThat(applicationRationaleFlareService.getFlaringLocations(applicationVersion))
        .hasSize(1)
        .first()
        .isEqualTo(ApplicationAssetView.from(terminalAsset));
  }
}
