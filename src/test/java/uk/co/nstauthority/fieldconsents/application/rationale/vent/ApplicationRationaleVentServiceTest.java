package uk.co.nstauthority.fieldconsents.application.rationale.vent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.InstanceOfAssertFactories.list;
import static org.assertj.core.api.InstanceOfAssertFactories.type;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field1AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.field2AssetJson;
import static uk.co.nstauthority.fieldconsents.assets.AssetTestUtil.terminal1AssetJson;
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
class ApplicationRationaleVentServiceTest {

  @Mock
  private ApplicationRationaleRepository repository;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ApplicationRationaleService applicationRationaleService;

  @InjectMocks
  private ApplicationRationaleVentService applicationRationaleVentService;

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
    var assetKeys = List.of(field1AssetJson.getAssetKey(), field2AssetJson.getAssetKey(), terminal1AssetJson.getAssetKey());
    var hostAssetKey = assetKeys.getFirst();

    applicationRationaleVentService.saveApplicationRationale(
        applicationVersion,
        rationaleType,
        null,
        assetKeys,
        hostAssetKey
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

    for (var assetKey : assetKeys) {
      verify(applicationAssetService).createAssetForApplicationVersion(
          applicationVersion,
          assetKey,
          AssetRole.LOCATION
      );
    }

    verify(applicationAssetService).createAssetForApplicationVersion(
        applicationVersion,
        hostAssetKey,
        AssetRole.HOST
    );
  }

  @Test
  void getSummaryCard_increase_withComment() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.INCREASE);
    applicationRationale.setComment("comment");

    getSummaryKeyValuesFrom(applicationRationaleVentService.getSummaryCard(applicationVersion))
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

    getSummaryKeyValuesFrom(applicationRationaleVentService.getSummaryCard(applicationVersion))
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

    getSummaryKeyValuesFrom(applicationRationaleVentService.getSummaryCard(applicationVersion))
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
    getSummaryKeyValuesFrom(applicationRationaleVentService.getSummaryCard(applicationVersion))
        .extracting(SummaryKeyValue::key, SummaryKeyValue::value)
        .contains(tuple("Where does the venting take place?", expectedLocationsString));
  }

  @Test
  void getSummaryCard_onlyVentingLocationData() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var locations = List.of(field1Json, field2Json, terminal1Json, terminal2Json);
    when(applicationRationaleService.getLocations(applicationVersion)).thenReturn(locations);

    var expectedLocationsString = "%s, %s, %s, %s".formatted(
        field1Json.getName(), field2Json.getName(), terminal1Json.getName(), terminal2Json.getName());

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.emptyList());

    assertThat(applicationRationaleVentService.getSummaryCard(applicationVersion))
        .isEqualTo(SummaryCard.simpleSummaryCard(
            SummaryDataView.newWithKeyValue("Where does the venting take place?", expectedLocationsString)
            )
        );
  }

  @Test
  void getSummaryCard_hostLocation() {
    when(applicationRationaleService.findByApplicationVersion(applicationVersion)).thenReturn(Optional.of(applicationRationale));

    applicationRationale.setRationaleType(ApplicationRationaleType.NO_CHANGE);

    when(applicationAssetService.getAssetJsonListFor(applicationVersion, AssetRole.HOST))
        .thenReturn(Collections.singletonList(field1Json));

    getSummaryKeyValuesFrom(applicationRationaleVentService.getSummaryCard(applicationVersion))
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

    assertThat(applicationRationaleVentService.getSummaryCard(applicationVersion))
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

    assertThat(applicationRationaleVentService.getSummaryCard(applicationVersion))
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
