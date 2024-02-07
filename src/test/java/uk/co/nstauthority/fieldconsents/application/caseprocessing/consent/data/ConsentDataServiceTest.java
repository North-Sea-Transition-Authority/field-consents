package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentEmissionFigureService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.document.FieldConsentsDocumentInstanceService;

@ExtendWith(MockitoExtension.class)
class ConsentDataServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ConsentDataRepository repository;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ConsentProductionFiguresService consentProductionFiguresService;

  @Mock
  private ConsentEmissionFigureService consentEmissionFigureService;

  @Mock
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @InjectMocks
  @Spy
  private ConsentDataService consentDataService;

  @Captor
  private ArgumentCaptor<ConsentData> consentDataCaptor;

  private Application application;

  @BeforeEach
  void setUp() {
    application = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE).getApplication();
  }

  @Test
  void findConsentData() {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(consentData));
    assertThat(consentDataService.findConsentData(application)).contains(consentData);
  }

  @Test
  void findConsentData_notFound() {
    when(repository.findByApplication(application)).thenReturn(Optional.empty());
    assertThat(consentDataService.findConsentData(application)).isEmpty();
  }

  @Test
  void saveConsentData_doesNotExistBeforeSaving() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    var form = ConsentDataForm.from(startDate, endDate);

    when(repository.findByApplication(application)).thenReturn(Optional.empty());

    consentDataService.saveConsentData(application, form);

    verify(fieldConsentsDocumentInstanceService).createDocumentInstancesForApplication(application);

    verify(repository).save(consentDataCaptor.capture());
    assertThat(consentDataCaptor.getValue())
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication,
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            null,
            application,
            startDate,
            endDate
        );
  }

  @Test
  void saveConsentData_doesExistBeforeSaving() {
    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    var form = ConsentDataForm.from(startDate, endDate);

    var existingConsentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(existingConsentData));

    consentDataService.saveConsentData(application, form);

    verify(fieldConsentsDocumentInstanceService, never()).createDocumentInstancesForApplication(any());

    verify(repository).save(consentDataCaptor.capture());
    assertThat(consentDataCaptor.getValue())
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication,
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            existingConsentData.getId(),
            application,
            startDate,
            endDate
        );
  }

  @Test
  void getPrefilledConsentDataForm_consentDataExists() {
    var consentData = ConsentDataTestUtil.newBuilder().build();

    doReturn(Optional.of(consentData)).when(consentDataService).findConsentData(application);

    assertThat(consentDataService.getPrefilledConsentDataForm(application))
        .extracting(
            form -> form.consentStartDate().getAsLocalDate().orElseThrow(),
            form -> form.consentEndDate().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate()
        );
  }

  @Test
  void getPrefilledConsentDataForm_consentDataDoesNotExist() {
    var applicationVersion = new ApplicationVersion();

    var consentLengthDetails = new ConsentLengthDetails();

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    doReturn(Optional.empty()).when(consentDataService).findConsentData(application);
    when(applicationVersionService.getLatestApplicationVersionByApplicationId(application.getId()))
        .thenReturn(applicationVersion);
    when(consentLengthService.getConsentLengthDetails(applicationVersion)).thenReturn(consentLengthDetails);
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);

    assertThat(consentDataService.getPrefilledConsentDataForm(application))
        .extracting(
            form -> form.consentStartDate().getAsLocalDate().orElseThrow(),
            form -> form.consentEndDate().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate
        );
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void getConsentDataView_applicationTypeIsProductionAndConsentLengthTypeIsShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentDataView = mock(ConsentDataView.class);

    doReturn(consentDataView)
        .when(consentDataService)
        .getConsentDataViewForShortTermOrAnnualProductionApplication(applicationVersion, consentData, consentLengthType);

    assertThat(consentDataService.getConsentDataView(applicationVersion, consentData, consentLengthType))
        .isEqualTo(consentDataView);
  }

  @Test
  void getConsentDataView_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentDataView = mock(ConsentDataView.class);

    doReturn(consentDataView)
        .when(consentDataService)
        .getConsentDataViewForLongTermProductionApplication(applicationVersion, consentData);

    assertThat(consentDataService.getConsentDataView(applicationVersion, consentData, ConsentLengthType.LONG_TERM))
        .isEqualTo(consentDataView);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void getConsentDataView_applicationTypeIsFlareOrVent(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var consentDataView = mock(ConsentDataView.class);

    doReturn(consentDataView)
        .when(consentDataService)
        .getConsentDataViewForFlareOrVentApplication(applicationVersion, consentData, consentLengthType);

    assertThat(consentDataService.getConsentDataView(applicationVersion, consentData, consentLengthType))
        .isEqualTo(consentDataView);
  }

  @Test
  void getConsentDataViewForShortTermOrAnnualProductionApplication_consentLengthTypeIsShortTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var shortTermConsentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    when(consentProductionFiguresService.getShortTermConsentProductionFiguresDto(applicationVersion))
        .thenReturn(shortTermConsentProductionFiguresDto);

    assertThat(
        consentDataService.getConsentDataViewForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentData,
            ConsentLengthType.SHORT_TERM
        )
    ).isEqualTo(
        ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData, shortTermConsentProductionFiguresDto)
    );
  }

  @Test
  void getConsentDataViewForShortTermOrAnnualProductionApplication_consentLengthTypeIsAnnual() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var annualConsentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    when(consentProductionFiguresService.getAnnualConsentProductionFiguresDto(applicationVersion))
        .thenReturn(annualConsentProductionFiguresDto);

    assertThat(
        consentDataService.getConsentDataViewForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentData,
            ConsentLengthType.ANNUAL
        )
    ).isEqualTo(ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData, annualConsentProductionFiguresDto));
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.EXCLUDE)
  void getConsentDataViewForShortTermOrAnnualProductionApplication_consentLengthTypeIsNotShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    assertThatThrownBy(() ->
        consentDataService.getConsentDataViewForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentData,
            consentLengthType
        )
    )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
  }

  @Test
  void getConsentDataViewForLongTermProductionApplication() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var longTermConsentProductionFiguresDtos = Map.of(2024, ConsentProductionFiguresDtoTestUtil.builder().build());

    when(consentProductionFiguresService.getLongTermConsentProductionFiguresDtos(applicationVersion))
        .thenReturn(longTermConsentProductionFiguresDtos);

    assertThat(consentDataService.getConsentDataViewForLongTermProductionApplication(applicationVersion, consentData))
        .isEqualTo(ConsentDataView.fromLongTermProductionApplication(consentData, longTermConsentProductionFiguresDtos));
  }

  @Test
  void getConsentDataViewForFlareOrVentApplication_consentLengthTypeIsShortTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var shortTermEmissionMaxRate = BigDecimal.valueOf(77.77);

    when(consentEmissionFigureService.getShortTermEmissionMaxRate(applicationVersion)).thenReturn(shortTermEmissionMaxRate);

    assertThat(consentDataService.getConsentDataViewForFlareOrVentApplication(applicationVersion, consentData, consentLengthType))
        .isEqualTo(ConsentDataView.fromFlareOrVentApplication(consentData, shortTermEmissionMaxRate));
  }

  @Test
  void getConsentDataViewForFlareOrVentApplication_consentLengthTypeIsAnnual() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentLengthType = ConsentLengthType.ANNUAL;

    var annualEmissionMaxRate = BigDecimal.valueOf(77.77);

    when(consentEmissionFigureService.getAnnualEmissionMaxRate(applicationVersion)).thenReturn(annualEmissionMaxRate);

    assertThat(consentDataService.getConsentDataViewForFlareOrVentApplication(applicationVersion, consentData, consentLengthType))
        .isEqualTo(ConsentDataView.fromFlareOrVentApplication(consentData, annualEmissionMaxRate));
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.EXCLUDE)
  void getConsentDataViewForFlareOrVentApplication_consentLengthTypeIsNotShortTermOrAnnual(ConsentLengthType consentLengthType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    assertThatThrownBy(() ->
        consentDataService.getConsentDataViewForFlareOrVentApplication(applicationVersion, consentData, consentLengthType)
    )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
  }
}
