package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2Json;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.assertj.core.api.ListAssert;
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
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetView;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleRepository;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleService;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class ApplicationRationaleFlareServiceTest {

  @Mock
  private ApplicationRationaleRepository repository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationRationaleService applicationRationaleService;

  @InjectMocks
  private ApplicationRationaleFlareService applicationRationaleFlareService;

  @Captor
  private ArgumentCaptor<ApplicationRationale> applicationRationaleCaptor;

  private ApplicationVersion applicationVersion;

  private ApplicationRationale applicationRationale;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    applicationRationale = new ApplicationRationale();
    applicationRationale.setApplicationVersion(applicationVersion);
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

    verify(repository).save(applicationRationaleCaptor.capture());
    assertThat(applicationRationaleCaptor.getValue())
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
  @EnumSource(ApplicationRationaleType.class)
  void getSummaryCard_rationaleType(ApplicationRationaleType applicationRationaleType) {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(applicationRationaleType);

    getSummaryKeyValuesFrom(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("Is this application for an increase or decrease?", applicationRationaleType.getDisplayName()));
  }

  @Test
  void getSummaryCard_increase_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(
            tuple("Is this application for an increase or decrease?", ApplicationRationaleType.INCREASE.getDisplayName()),
            tuple("Why are you asking for an increase?", "comment")
        );
  }

  @Test
  void getSummaryCard_decrease_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.DECREASE);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(
            tuple("Is this application for an increase or decrease?", ApplicationRationaleType.DECREASE.getDisplayName()),
            tuple("Why are you asking for a decrease?", "comment")
        );
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationRationaleType.class, names = {"INCREASE", "DECREASE"}, mode = EXCLUDE)
  void getSummaryCard_nonIncreaseOrDecrease_withComment(ApplicationRationaleType applicationRationaleType) {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(applicationRationaleType);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("Is this application for an increase or decrease?", applicationRationaleType.getDisplayName()));
  }

  @Test
  void getSummaryCard_nonHostLocations() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.NO_CHANGE);

    var locations = List.of(field1Json, field2Json, terminal1Json, terminal2Json);
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(locations);

    var expectedLocationsString = locations
        .stream()
        .map(assetJson -> ApplicationAssetView.from(assetJson).getName())
        .collect(Collectors.joining(", "));

    getSummaryKeyValuesFrom(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("Where does the flaring take place?", expectedLocationsString));
  }

  @Test
  void getSummaryCard_onlyFlaringLocationData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var locations = List.of(field1Json, field2Json, terminal1Json, terminal2Json);
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(locations);

    var expectedLocationsString = "%s, %s, %s, %s".formatted(
        field1Json.getName(), field2Json.getName(), terminal1Json.getName(), terminal2Json.getName());

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.emptyList());

    assertThat(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.simpleSummaryCard(
                SummaryDataView.newWithKeyValue("Where does the flaring take place?", expectedLocationsString)
            )
        );
  }

  @Test
  void getSummaryCard_hostLocation() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.NO_CHANGE);

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(field1Json));

    getSummaryKeyValuesFrom(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("What is the host?", field1Json.getSelectionText()));
  }

  @Test
  void getSummaryCard_onlyHostLocationData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(field1Json));

    assertThat(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.simpleSummaryCard(
                SummaryDataView.newWithKeyValue("What is the host?", field1Json.getSelectionText())
            )
        );
  }

  @Test
  void getSummaryCard_noData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());
    when(applicationRationaleService.getLocations(applicationVersion))
        .thenReturn(Collections.emptyList());
    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.emptyList());

    assertThat(applicationRationaleFlareService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.emptySummaryCard());
  }


  private ListAssert<SummaryKeyValue> getSummaryKeyValuesFrom(SummaryCard summaryCard) {
    return assertThat(summaryCard)
        .extracting(SummaryCard::summaryData)
        .asInstanceOf(type(SummaryDataView.class))
        .extracting(SummaryDataView::keyValues)
        .asInstanceOf(list(SummaryKeyValue.class));
  }
}
