package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils.bigDecimalToFormattedString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentDataLongTermProductionFiguresTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentEmissionFigureService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDto;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresDtoTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.figure.ConsentProductionFiguresView;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthDetails;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthService;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;
import uk.co.nstauthority.fieldconsents.util.StreamUtils;

@ExtendWith(MockitoExtension.class)
class ConsentDataServiceTest {

  @Mock
  private ConsentDataRepository repository;

  @Mock
  private ConsentLengthService consentLengthService;

  @Mock
  private ConsentProductionFiguresService consentProductionFiguresService;

  @Mock
  private ConsentEmissionFigureService consentEmissionFigureService;

  @Mock
  private ConsentDataLongTermProductionFiguresService consentDataLongTermProductionFiguresService;

  @InjectMocks
  @Spy
  private ConsentDataService consentDataService;

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Captor
  private ArgumentCaptor<ConsentData> consentDataCaptor;

  @Test
  void findConsentData() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(consentData));
    assertThat(consentDataService.findConsentData(application)).contains(consentData);
  }

  @Test
  void findConsentData_notFound() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    when(repository.findByApplication(application)).thenReturn(Optional.empty());
    assertThat(consentDataService.findConsentData(application)).isEmpty();
  }

  @Test
  void getConsentData() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    doReturn(Optional.of(consentData)).when(consentDataService).findConsentData(application);

    assertThat(consentDataService.getConsentData(application)).isEqualTo(consentData);
  }

  @Test
  void getConsentDataListInRangeForCompletedProductionApplicationsByFieldId() {
    var start = LocalDate.now();
    var end = start.plusDays(1);
    var fieldIds = Set.of(1, 2, 3);

    var field1ConsentData1 = ConsentDataTestUtil.newBuilder().build();
    var field1ConsentData2 = ConsentDataTestUtil.newBuilder().build();
    var field2ConsentData1 = ConsentDataTestUtil.newBuilder().build();
    // field 3 doesn't have any consent data therefore it's not included here

    when(repository.getConsentDataListInRangeForCompletedProductionApplicationsForFieldIds(start, end, fieldIds))
        .thenReturn(List.of(
            new ConsentDataByFieldIdTestImpl(1, field1ConsentData1),
            new ConsentDataByFieldIdTestImpl(1, field1ConsentData2),
            new ConsentDataByFieldIdTestImpl(2, field2ConsentData1)
        ));

    assertThat(
        consentDataService.getConsentDataListInRangeForCompletedProductionApplicationsByFieldId(start, end, fieldIds))
        .containsExactlyInAnyOrderEntriesOf(Map.of(
            1, List.of(field1ConsentData1, field1ConsentData2),
            2, List.of(field2ConsentData1)
        ));
  }

  @Test
  void getConsentData_notFound() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    doReturn(Optional.empty()).when(consentDataService).findConsentData(application);

    assertThatThrownBy(() -> consentDataService.getConsentData(application))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unable to find consent data for application: %s".formatted(application.getId()));
  }

  @Test
  void onConsentLengthChangeEvent() {
    var application = new Application();

    var applicationVersionId = 1;
    var applicationVersion = new ApplicationVersion();
    applicationVersion.setApplication(application);

    var event = new ConsentLengthChangeEvent(this.getClass(), applicationVersionId);

    when(applicationVersionService.getApplicationVersionById(applicationVersionId)).thenReturn(applicationVersion);

    consentDataService.onConsentLengthChangeEvent(event);

    verify(consentDataLongTermProductionFiguresService).deleteConsentDataLongTermProductionFigures(application);
    verify(repository).deleteByApplication(application);
  }

  @Test
  void saveConsentData_doesNotExistBeforeSaving() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var form = new ConsentDataForm();
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    when(repository.findByApplication(application)).thenReturn(Optional.empty());
    doNothing().when(consentDataService).updateConsentDataFromForm(any(), any(), any(), any());

    consentDataService.saveConsentData(application, consentLengthType, form);

    verify(consentDataService)
        .updateConsentDataFromForm(eq(application), eq(consentLengthType), consentDataCaptor.capture(), eq(form));

    var consentData = consentDataCaptor.getValue();

    assertThat(consentData)
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication
        ).containsExactly(
            null,
            application
        );

    verify(repository).save(consentData);

    verify(consentDataLongTermProductionFiguresService, never()).saveConsentDataLongTermProductionFigures(application, form);
  }

  @Test
  void saveConsentData_doesNotExistBeforeSaving_applicationTypeIsProductionAndConsentLengthIsLongTerm() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var form = new ConsentDataForm();
    var consentLengthType = ConsentLengthType.LONG_TERM;

    when(repository.findByApplication(application)).thenReturn(Optional.empty());
    doNothing().when(consentDataService).updateConsentDataFromForm(any(), any(), any(), any());

    consentDataService.saveConsentData(application, consentLengthType, form);

    verify(consentDataService)
        .updateConsentDataFromForm(eq(application), eq(consentLengthType), consentDataCaptor.capture(), eq(form));

    var consentData = consentDataCaptor.getValue();

    assertThat(consentData)
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication
        ).containsExactly(
            null,
            application
        );

    verify(repository).save(consentData);

    verify(consentDataLongTermProductionFiguresService).saveConsentDataLongTermProductionFigures(application, form);
  }

  @Test
  void saveConsentData_doesExistBeforeSaving() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var form = new ConsentDataForm();
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var existingConsentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(existingConsentData));
    doNothing().when(consentDataService).updateConsentDataFromForm(any(), any(), any(), any());

    consentDataService.saveConsentData(application, consentLengthType, form);

    verify(consentDataService)
        .updateConsentDataFromForm(application, consentLengthType, existingConsentData, form);

    assertThat(existingConsentData)
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication
        ).containsExactly(
            existingConsentData.getId(),
            application
        );

    verify(repository).save(existingConsentData);

    verify(consentDataLongTermProductionFiguresService, never()).saveConsentDataLongTermProductionFigures(application, form);
  }

  @Test
  void saveConsentData_doesExistBeforeSaving_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var form = new ConsentDataForm();
    var consentLengthType = ConsentLengthType.LONG_TERM;

    var existingConsentData = ConsentDataTestUtil.newBuilder().build();

    when(repository.findByApplication(application)).thenReturn(Optional.of(existingConsentData));
    doNothing().when(consentDataService).updateConsentDataFromForm(any(), any(), any(), any());

    consentDataService.saveConsentData(application, consentLengthType, form);

    verify(consentDataService)
        .updateConsentDataFromForm(application, consentLengthType, existingConsentData, form);

    assertThat(existingConsentData)
        .extracting(
            ConsentData::getId,
            ConsentData::getApplication
        ).containsExactly(
            existingConsentData.getId(),
            application
        );

    verify(repository).save(existingConsentData);

    verify(consentDataLongTermProductionFiguresService).saveConsentDataLongTermProductionFigures(application, form);
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void updateConsentDataFromForm_applicationTypeIsProductionAndConsentLengthTypeIsShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentData = new ConsentData();

    var form = new ConsentDataForm();

    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    form.getConsentStartDateInput().setDate(startDate);
    form.getConsentEndDateInput().setDate(endDate);

    doNothing().when(consentDataService).updateConsentDataFromFormForShortTermOrAnnualProductionApplication(any(), any());

    consentDataService.updateConsentDataFromForm(application, consentLengthType, consentData, form);

    assertThat(consentData)
        .extracting(
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            startDate,
            endDate
        );

    verify(consentDataService).updateConsentDataFromFormForShortTermOrAnnualProductionApplication(consentData, form);
  }

  @Test
  void updateConsentDataFromForm_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentData = new ConsentData();

    var form = new ConsentDataForm();

    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    form.getConsentStartDateInput().setDate(startDate);
    form.getConsentEndDateInput().setDate(endDate);

    doNothing().when(consentDataService).updateConsentDataFromFormForLongTermProductionApplication(any(), any());

    consentDataService.updateConsentDataFromForm(application, ConsentLengthType.LONG_TERM, consentData, form);

    assertThat(consentData)
        .extracting(
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            startDate,
            endDate
        );

    verify(consentDataService).updateConsentDataFromFormForLongTermProductionApplication(consentData, form);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void updateConsentDataFromForm_applicationTypeIsFlareOrVent(ApplicationType applicationType) {
    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentLengthType = ConsentLengthType.SHORT_TERM;
    var consentData = new ConsentData();

    var form = new ConsentDataForm();

    var startDate = LocalDate.parse("2024-01-01");
    var endDate = LocalDate.parse("2025-01-01");

    form.getConsentStartDateInput().setDate(startDate);
    form.getConsentEndDateInput().setDate(endDate);

    doNothing().when(consentDataService).updateConsentDataFromFormForEmissionApplication(any(), any());

    consentDataService.updateConsentDataFromForm(application, consentLengthType, consentData, form);

    assertThat(consentData)
        .extracting(
            ConsentData::getConsentStartDate,
            ConsentData::getConsentEndDate
        ).containsExactly(
            startDate,
            endDate
        );

    verify(consentDataService).updateConsentDataFromFormForEmissionApplication(consentData, form);
  }

  @Test
  void updateConsentDataFromFormForShortTermOrAnnualProductionApplication() {
    var consentData = new ConsentData();
    var form = new ConsentDataForm();

    var consentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    form.getShortTermOrAnnualConsentProductionFiguresInput().setInputValuesFromDto(consentProductionFiguresDto);

    consentDataService.updateConsentDataFromFormForShortTermOrAnnualProductionApplication(consentData, form);

    assertThat(consentData)
        .extracting(
            ConsentData::getShortTermOrAnnualProductionMinOil,
            ConsentData::getShortTermOrAnnualProductionMaxOil,
            ConsentData::getShortTermOrAnnualProductionMinGas,
            ConsentData::getShortTermOrAnnualProductionMaxGas
        )
        .containsExactly(
            consentProductionFiguresDto.minOil(),
            consentProductionFiguresDto.maxOil(),
            consentProductionFiguresDto.minGas(),
            consentProductionFiguresDto.maxGas()
        );
  }

  @Test
  void updateConsentDataFromFormForLongTermProductionApplication() {
    var consentData = new ConsentData();
    var form = new ConsentDataForm();

    var longTermProductionConsentProductionFromDate = LocalDate.parse("2024-02-23");

    form.getLongTermProductionConsentProductionFromDateInput().setDate(longTermProductionConsentProductionFromDate);

    consentDataService.updateConsentDataFromFormForLongTermProductionApplication(consentData, form);

    assertThat(consentData.getLongTermProductionConsentProductionFromDate())
        .isEqualTo(longTermProductionConsentProductionFromDate);
  }

  @Test
  void updateConsentDataFromFormForEmissionApplication() {
    var consentData = new ConsentData();
    var form = new ConsentDataForm();

    var emissionDailyAverage = BigDecimal.valueOf(873.46);

    form.getEmissionDailyAverageInput().setInputValue(bigDecimalToFormattedString(emissionDailyAverage));

    consentDataService.updateConsentDataFromFormForEmissionApplication(consentData, form);

    assertThat(consentData.getEmissionDailyAverage()).isEqualTo(emissionDailyAverage);
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void getPrefilledConsentDataForm_applicationTypeIsProductionAndConsentLengthTypeIsShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var prefilledForm = new ConsentDataForm();

    doReturn(prefilledForm)
        .when(consentDataService)
        .getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(applicationVersion, consentLengthDetails);

    assertThat(consentDataService.getPrefilledConsentDataForm(applicationVersion, consentLengthDetails)).isEqualTo(prefilledForm);
  }

  @Test
  void getPrefilledConsentDataForm_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    var prefilledForm = new ConsentDataForm();

    doReturn(prefilledForm)
        .when(consentDataService)
        .getPrefilledConsentDataFormForLongTermProductionApplication(applicationVersion, consentLengthDetails);

    assertThat(consentDataService.getPrefilledConsentDataForm(applicationVersion, consentLengthDetails)).isEqualTo(prefilledForm);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void getPrefilledConsentDataForm_applicationTypeIsFlareOrVent(ApplicationType applicationType) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(applicationType);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var prefilledForm = new ConsentDataForm();

    doReturn(prefilledForm)
        .when(consentDataService)
        .getPrefilledConsentDataFormForEmissionApplication(applicationVersion, consentLengthDetails);

    assertThat(consentDataService.getPrefilledConsentDataForm(applicationVersion, consentLengthDetails)).isEqualTo(prefilledForm);
  }

  @Test
  void getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication_consentDataExists() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var shortTermOrAnnualProductionMinOil = BigDecimal.valueOf(235.79);
    var shortTermOrAnnualProductionMaxOil = BigDecimal.valueOf(673.12);
    var shortTermOrAnnualProductionMinGas = BigDecimal.valueOf(112.89);
    var shortTermOrAnnualProductionMaxGas = BigDecimal.valueOf(456.99);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withShortTermOrAnnualProductionMinOil(shortTermOrAnnualProductionMinOil)
        .withShortTermOrAnnualProductionMaxOil(shortTermOrAnnualProductionMaxOil)
        .withShortTermOrAnnualProductionMinGas(shortTermOrAnnualProductionMinGas)
        .withShortTermOrAnnualProductionMaxGas(shortTermOrAnnualProductionMaxGas)
        .build();

    doReturn(Optional.of(consentData)).when(consentDataService).findConsentData(applicationVersion.getApplication());

    assertThat(
        consentDataService.getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getShortTermOrAnnualConsentProductionFiguresInput().getAsDtoOrThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate(),
            new ConsentProductionFiguresDto(
                shortTermOrAnnualProductionMinOil,
                shortTermOrAnnualProductionMaxOil,
                shortTermOrAnnualProductionMinGas,
                shortTermOrAnnualProductionMaxGas
            )
        );
  }

  @Test
  void getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication_consentDataDoesNotExist_consentLengthTypeIsShortTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    var shortTermConsentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);
    when(consentProductionFiguresService.getShortTermConsentProductionFiguresDto(applicationVersion))
        .thenReturn(shortTermConsentProductionFiguresDto);

    assertThat(
        consentDataService.getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getShortTermOrAnnualConsentProductionFiguresInput().getAsDtoOrThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate,
            shortTermConsentProductionFiguresDto
        );
  }

  @Test
  void getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication_consentDataDoesNotExist_consentLengthTypeIsAnnual() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.ANNUAL);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    var annualConsentProductionFiguresDto = ConsentProductionFiguresDtoTestUtil.builder().build();

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);
    when(consentProductionFiguresService.getAnnualConsentProductionFiguresDto(applicationVersion))
        .thenReturn(annualConsentProductionFiguresDto);

    assertThat(
        consentDataService.getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getShortTermOrAnnualConsentProductionFiguresInput().getAsDtoOrThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate,
            annualConsentProductionFiguresDto
        );
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.EXCLUDE)
  void getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication_consentDataDoesNotExist_consentLengthTypeIsNotShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);

    assertThatThrownBy(() ->
        consentDataService.getPrefilledConsentDataFormForShortTermOrAnnualProductionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
  }

  @Test
  void getPrefilledConsentDataFormForLongTermProductionApplication_consentDataExists() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    var application = applicationVersion.getApplication();

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentDataLongTermProductionFigures2024 = ConsentDataLongTermProductionFiguresTestUtil.builder()
        .withYear(2024)
        .withMinOil(BigDecimal.valueOf(428.76))
        .withMaxOil(BigDecimal.valueOf(714.23))
        .withMinGas(BigDecimal.valueOf(189.47))
        .withMaxGas(BigDecimal.valueOf(837.14))
        .build();
    var consentDataLongTermProductionFigures2025 = ConsentDataLongTermProductionFiguresTestUtil.builder()
        .withYear(2025)
        .withMinOil(BigDecimal.valueOf(583.24))
        .withMaxOil(BigDecimal.valueOf(327.89))
        .withMinGas(BigDecimal.valueOf(901.45))
        .withMaxGas(BigDecimal.valueOf(124.56))
        .build();
    var consentDataLongTermProductionFigures2026 = ConsentDataLongTermProductionFiguresTestUtil.builder()
        .withYear(2026)
        .withMinOil(BigDecimal.valueOf(241.57))
        .withMaxOil(BigDecimal.valueOf(789.32))
        .withMinGas(BigDecimal.valueOf(456.28))
        .withMaxGas(BigDecimal.valueOf(602.11))
        .build();
    var consentDataLongTermProductionFigures2027 = ConsentDataLongTermProductionFiguresTestUtil.builder()
        .withYear(2027)
        .withMinOil(BigDecimal.valueOf(147.83))
        .withMaxOil(BigDecimal.valueOf(562.39))
        .withMinGas(BigDecimal.valueOf(378.21))
        .withMaxGas(BigDecimal.valueOf(943.67))
        .build();
    var consentDataLongTermProductionFigures2028 = ConsentDataLongTermProductionFiguresTestUtil.builder()
        .withYear(2028)
        .withMinOil(BigDecimal.valueOf(689.45))
        .withMaxOil(BigDecimal.valueOf(235.78))
        .withMinGas(BigDecimal.valueOf(501.92))
        .withMaxGas(BigDecimal.valueOf(789.34))
        .build();

    var consentDataLongTermProductionFiguresList = List.of(
        consentDataLongTermProductionFigures2026,
        consentDataLongTermProductionFigures2028,
        consentDataLongTermProductionFigures2025,
        consentDataLongTermProductionFigures2024,
        consentDataLongTermProductionFigures2027
    );

    doReturn(Optional.of(consentData)).when(consentDataService).findConsentData(applicationVersion.getApplication());

    when(consentDataLongTermProductionFiguresService.getConsentDataLongTermProductionFiguresList(application))
        .thenReturn(consentDataLongTermProductionFiguresList);

    var form = consentDataService.getPrefilledConsentDataFormForLongTermProductionApplication(
        applicationVersion,
        consentLengthDetails
    );

    assertThat(form)
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate()
        );

    assertThat(
        form.getLongTermConsentProductionFiguresInputs()
            .entrySet()
            .stream()
            .collect(StreamUtils.toLinkedHashMap(Map.Entry::getKey, entry -> entry.getValue().getAsDtoOrThrow()))
    )
        .containsExactly(
            entry("2024", ConsentProductionFiguresDto.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures2024)),
            entry("2025", ConsentProductionFiguresDto.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures2025)),
            entry("2026", ConsentProductionFiguresDto.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures2026)),
            entry("2027", ConsentProductionFiguresDto.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures2027)),
            entry("2028", ConsentProductionFiguresDto.fromConsentDataLongTermProductionFigures(consentDataLongTermProductionFigures2028))
        );
  }

  @Test
  void getPrefilledConsentDataFormForLongTermProductionApplication_consentDataDoesNotExist() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.LONG_TERM);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    var consentProductionFiguresDto2024 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(428.76))
        .withMaxOil(BigDecimal.valueOf(714.23))
        .withMinGas(BigDecimal.valueOf(189.47))
        .withMaxGas(BigDecimal.valueOf(837.14))
        .build();
    var consentProductionFiguresDto2025 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(583.24))
        .withMaxOil(BigDecimal.valueOf(327.89))
        .withMinGas(BigDecimal.valueOf(901.45))
        .withMaxGas(BigDecimal.valueOf(124.56))
        .build();
    var consentProductionFiguresDto2026 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(241.57))
        .withMaxOil(BigDecimal.valueOf(789.32))
        .withMinGas(BigDecimal.valueOf(456.28))
        .withMaxGas(BigDecimal.valueOf(602.11))
        .build();
    var consentProductionFiguresDto2027 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(147.83))
        .withMaxOil(BigDecimal.valueOf(562.39))
        .withMinGas(BigDecimal.valueOf(378.21))
        .withMaxGas(BigDecimal.valueOf(943.67))
        .build();
    var consentProductionFiguresDto2028 = ConsentProductionFiguresDtoTestUtil.builder()
        .withMinOil(BigDecimal.valueOf(689.45))
        .withMaxOil(BigDecimal.valueOf(235.78))
        .withMinGas(BigDecimal.valueOf(501.92))
        .withMaxGas(BigDecimal.valueOf(789.34))
        .build();

    var longTermConsentProductionFiguresDtos = Map.of(
        2026, consentProductionFiguresDto2026,
        2028, consentProductionFiguresDto2028,
        2025, consentProductionFiguresDto2025,
        2024, consentProductionFiguresDto2024,
        2027, consentProductionFiguresDto2027
    );

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);
    when(consentProductionFiguresService.getLongTermConsentProductionFiguresDtos(applicationVersion))
        .thenReturn(longTermConsentProductionFiguresDtos);

    var form = consentDataService.getPrefilledConsentDataFormForLongTermProductionApplication(
        applicationVersion,
        consentLengthDetails
    );

    assertThat(form)
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate
        );

    assertThat(
        form.getLongTermConsentProductionFiguresInputs()
            .entrySet()
            .stream()
            .collect(StreamUtils.toLinkedHashMap(Map.Entry::getKey, entry -> entry.getValue().getAsDtoOrThrow()))
    )
        .containsExactly(
            entry("2024", consentProductionFiguresDto2024),
            entry("2025", consentProductionFiguresDto2025),
            entry("2026", consentProductionFiguresDto2026),
            entry("2027", consentProductionFiguresDto2027),
            entry("2028", consentProductionFiguresDto2028)
        );
  }

  @Test
  void getPrefilledConsentDataFormForEmissionApplication_consentDataExists() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var emissionDailyAverage = BigDecimal.valueOf(873.46);

    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(emissionDailyAverage)
        .build();

    doReturn(Optional.of(consentData)).when(consentDataService).findConsentData(applicationVersion.getApplication());

    assertThat(
        consentDataService.getPrefilledConsentDataFormForEmissionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getEmissionDailyAverageInput().getAsBigDecimal().orElseThrow()
        )
        .containsExactly(
            consentData.getConsentStartDate(),
            consentData.getConsentEndDate(),
            emissionDailyAverage
        );
  }

  @Test
  void getPrefilledConsentDataFormForEmissionApplication_consentDataDoesNotExist_consentLengthTypeIsShortTerm() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.SHORT_TERM);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    var shortTermEmissionDailyAverage = BigDecimal.valueOf(77.77);

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);
    when(consentEmissionFigureService.getShortTermEmissionDailyAverage(applicationVersion))
        .thenReturn(shortTermEmissionDailyAverage);

    assertThat(
        consentDataService.getPrefilledConsentDataFormForEmissionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getEmissionDailyAverageInput().getAsBigDecimal().orElseThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate,
            shortTermEmissionDailyAverage
        );
  }

  @Test
  void getPrefilledConsentDataFormForEmissionApplication_consentDataDoesNotExist_consentLengthTypeIsAnnual() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(ConsentLengthType.ANNUAL);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    var annualEmissionDailyAverage = BigDecimal.valueOf(77.77);

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);
    when(consentEmissionFigureService.getAnnualEmissionDailyAverage(applicationVersion))
        .thenReturn(annualEmissionDailyAverage);

    assertThat(
        consentDataService.getPrefilledConsentDataFormForEmissionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .extracting(
            consentDataForm -> consentDataForm.getConsentStartDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getConsentEndDateInput().getAsLocalDate().orElseThrow(),
            consentDataForm -> consentDataForm.getEmissionDailyAverageInput().getAsBigDecimal().orElseThrow()
        )
        .containsExactly(
            proposedConsentStartDate,
            proposedConsentEndDate,
            annualEmissionDailyAverage
        );
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.EXCLUDE)
  void getPrefilledConsentDataFormForEmissionApplication_consentDataDoesNotExist_consentLengthTypeIsNotShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);

    var consentLengthDetails = new ConsentLengthDetails();
    consentLengthDetails.setConsentLength(consentLengthType);

    var proposedConsentStartDate = LocalDate.parse("2024-01-01");
    var proposedConsentEndDate = LocalDate.parse("2025-01-01");

    doReturn(Optional.empty()).when(consentDataService).findConsentData(applicationVersion.getApplication());
    when(consentLengthService.getProposedConsentStartDate(consentLengthDetails)).thenReturn(proposedConsentStartDate);
    when(consentLengthService.getProposedConsentEndDate(consentLengthDetails)).thenReturn(proposedConsentEndDate);

    assertThatThrownBy(() ->
        consentDataService.getPrefilledConsentDataFormForEmissionApplication(
            applicationVersion,
            consentLengthDetails
        )
    )
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Unexpected ConsentLengthType: %s".formatted(consentLengthType));
  }

  @ParameterizedTest
  @EnumSource(value = ConsentLengthType.class, names = { "SHORT_TERM", "ANNUAL" }, mode = EnumSource.Mode.INCLUDE)
  void getConsentDataView_applicationTypeIsProductionAndConsentLengthTypeIsShortTermOrAnnual(
      ConsentLengthType consentLengthType
  ) {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentDataView = mock(ConsentDataView.class);

    doReturn(consentDataView)
        .when(consentDataService)
        .getConsentDataViewForShortTermOrAnnualProductionApplication(consentData);

    assertThat(consentDataService.getConsentDataView(application, consentData, consentLengthType)).isEqualTo(consentDataView);
  }

  @Test
  void getConsentDataView_applicationTypeIsProductionAndConsentLengthTypeIsLongTerm() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentLengthType = ConsentLengthType.LONG_TERM;

    var consentDataView = mock(ConsentDataView.class);

    doReturn(consentDataView)
        .when(consentDataService)
        .getConsentDataViewForLongTermProductionApplication(application, consentData);

    assertThat(consentDataService.getConsentDataView(application, consentData, consentLengthType)).isEqualTo(consentDataView);
  }

  @ParameterizedTest
  @EnumSource(value = ApplicationType.class, names = { "FLARE", "VENT" }, mode = EnumSource.Mode.INCLUDE)
  void getConsentDataView_applicationTypeIsFlareOrVent(ApplicationType applicationType) {
    var application = ApplicationTestUtil.getNewApplicationWithType(applicationType);
    var consentData = ConsentDataTestUtil.newBuilder().build();
    var consentLengthType = ConsentLengthType.SHORT_TERM;

    var consentDataView = mock(ConsentDataView.class);

    doReturn(consentDataView)
        .when(consentDataService)
        .getConsentDataViewForEmissionApplication(consentData);

    assertThat(consentDataService.getConsentDataView(application, consentData, consentLengthType)).isEqualTo(consentDataView);
  }

  @Test
  void getConsentDataViewForShortTermOrAnnualProductionApplication() {
    var consentData = ConsentDataTestUtil.newBuilder()
        .withShortTermOrAnnualProductionMinOil(BigDecimal.valueOf(428.76))
        .withShortTermOrAnnualProductionMaxOil(BigDecimal.valueOf(714.23))
        .withShortTermOrAnnualProductionMinGas(BigDecimal.valueOf(189.47))
        .withShortTermOrAnnualProductionMaxGas(BigDecimal.valueOf(837.14))
        .build();

    assertThat(consentDataService.getConsentDataViewForShortTermOrAnnualProductionApplication(consentData))
        .isEqualTo(ConsentDataView.fromShortTermOrAnnualProductionApplication(consentData));
  }

  @Test
  void getConsentDataViewForLongTermProductionApplication() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consentData = ConsentDataTestUtil.newBuilder().build();

    var consentDataLongTermProductionFiguresViews = Map.of(
        "2024", mock(ConsentProductionFiguresView.class),
        "2025", mock(ConsentProductionFiguresView.class)
    );

    when(consentDataLongTermProductionFiguresService.getConsentDataLongTermProductionFiguresViews(application))
        .thenReturn(consentDataLongTermProductionFiguresViews);

    assertThat(consentDataService.getConsentDataViewForLongTermProductionApplication(application, consentData))
        .isEqualTo(ConsentDataView.fromLongTermProductionApplication(consentData, consentDataLongTermProductionFiguresViews));
  }

  @Test
  void getConsentDataViewForEmissionApplication() {
    var consentData = ConsentDataTestUtil.newBuilder()
        .withEmissionDailyAverage(BigDecimal.valueOf(235.79))
        .build();

    assertThat(consentDataService.getConsentDataViewForEmissionApplication(consentData))
        .isEqualTo(ConsentDataView.fromEmissionApplication(consentData));
  }
}
