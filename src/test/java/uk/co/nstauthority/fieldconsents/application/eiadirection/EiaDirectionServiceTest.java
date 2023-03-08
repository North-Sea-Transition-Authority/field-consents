package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionTestUtil.TOMORROW;
import static uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirectionTestUtil.WHY_NO_EIA_DIRECTION;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_3;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_REF_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_REF_3;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication3Json;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class EiaDirectionServiceTest {

  private static final String HAVE_SUBMITTED_EIA_DIRECTION_PROMPT = "Have you submitted an EIA screening direction?";

  private static final String EIA_DIRECTION_REF_PROMPT = "EIA screening direction reference";

  private static final String HAVE_EIA_DIRECTION_TO_SUBMIT_PROMPT =
      "Do you have an EIA screening direction that still needs to be submitted?";

  private static final String LATEST_DATE_TO_SUBMIT_PROMPT = "What is the latest date this will be submitted?";

  private static final String EXPLANATION_PROMPT = "Explain why you don’t intend to submit an EIA screening direction";

  @Mock
  private EiaDirectionRepository eiaDirectionRepository;

  @Mock
  private PetsApplicationService petsApplicationService;

  @InjectMocks
  private EiaDirectionService eiaDirectionService;

  ApplicationVersion applicationVersion;

  EiaDirection testEiaDirection;

  EiaDirectionForm testEiaDirectionForm;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    testEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSatToSubmit(applicationVersion);
    testEiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSatToSubmit();
  }

  @Test
  void findEiaDirection_notFound() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var eiaDirectionOptional = eiaDirectionService.findEiaDirection(applicationVersion);
    assertThat(eiaDirectionOptional).isEmpty();
  }

  @Test
  void findEiaDirection_found() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(testEiaDirection));

    var eiaDirectionOptional = eiaDirectionService.findEiaDirection(applicationVersion);
    assertThat(eiaDirectionOptional).contains(testEiaDirection);
  }


  @Test
  void getEiaDirectionForm_notFound() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var eiaDirectionForm = eiaDirectionService.getEiaDirectionForm(applicationVersion);
    assertThat(eiaDirectionForm)
        .usingRecursiveComparison()
        .isEqualTo(new EiaDirectionForm());
  }

  @Test
  void getEiaDirectionForm_found() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(testEiaDirection));

    var eiaDirectionForm = eiaDirectionService.getEiaDirectionForm(applicationVersion);
    assertThat(eiaDirectionForm)
        .usingRecursiveComparison()
        .isEqualTo(testEiaDirectionForm);
  }


  @Test
  void saveEiaDirection_noSatId() {
    eiaDirectionService.saveEiaDirection(applicationVersion, testEiaDirectionForm);
    verify(eiaDirectionRepository, times(1)).deleteByApplicationVersion(applicationVersion);
    ArgumentCaptor<EiaDirection> eiaDirectionArgumentCaptor = ArgumentCaptor.forClass(EiaDirection.class);
    verify(eiaDirectionRepository, times(1)).save(eiaDirectionArgumentCaptor.capture());

    assertThat(eiaDirectionArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(testEiaDirection);
  }

  @Test
  void saveEiaDirection_satIdExistPetsAppFound() {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSat(SAT_ID_3);
    var eiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSat(applicationVersion, SAT_ID_3, SAT_REF_3);

    when(petsApplicationService.getPetsApplicationById(SAT_ID_3, EiaDirectionService.LOOKUP_SAT_REF_PURPOSE))
        .thenReturn(petsApplication3Json);

    eiaDirectionService.saveEiaDirection(applicationVersion, eiaDirectionForm);
    verify(eiaDirectionRepository, times(1)).deleteByApplicationVersion(applicationVersion);
    ArgumentCaptor<EiaDirection> eiaDirectionArgumentCaptor = ArgumentCaptor.forClass(EiaDirection.class);
    verify(eiaDirectionRepository, times(1)).save(eiaDirectionArgumentCaptor.capture());

    assertThat(eiaDirectionArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(eiaDirection);
  }

  @Test
  void saveEiaDirection_satIdExistPetsAppNotFound() {
    var eiaDirectionForm = EiaDirectionTestUtil.getEiaDirectionFormWithSat(SAT_ID_3);

    when(petsApplicationService.getPetsApplicationById(SAT_ID_3, EiaDirectionService.LOOKUP_SAT_REF_PURPOSE))
        .thenThrow(EntityNotFoundException.class);

    assertThatThrownBy(() -> eiaDirectionService.saveEiaDirection(applicationVersion, eiaDirectionForm))
        .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  void getSummaryView_noEiaDataFound() {
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.empty());

    var summaryDataView = eiaDirectionService.getEiaDirectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView)
        .usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(new SummaryKeyValue(HAVE_SUBMITTED_EIA_DIRECTION_PROMPT, null))
        ));
  }

  @Test
  void getSummaryView_withSat() {
    testEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSat(applicationVersion, SAT_ID_3, SAT_REF_3);
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(testEiaDirection));
    when(petsApplicationService.getPetsApplicationById(SAT_ID_3, EiaDirectionService.LOOKUP_SAT_REF_PURPOSE))
        .thenReturn(petsApplication3Json);

    var summaryDataView = eiaDirectionService.getEiaDirectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView)
        .usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(
                new SummaryKeyValue(HAVE_SUBMITTED_EIA_DIRECTION_PROMPT, "Yes"),
                new SummaryKeyValue(EIA_DIRECTION_REF_PROMPT, SAT_REF_3)
            )
        ));
  }

  @Test
  void getSummaryView_withSatToSubmit() {
    testEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithSatToSubmit(applicationVersion);
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(testEiaDirection));

    var summaryDataView = eiaDirectionService.getEiaDirectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView)
        .usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(
                new SummaryKeyValue(HAVE_SUBMITTED_EIA_DIRECTION_PROMPT, "No"),
                new SummaryKeyValue(HAVE_EIA_DIRECTION_TO_SUBMIT_PROMPT, "Yes"),
                new SummaryKeyValue(LATEST_DATE_TO_SUBMIT_PROMPT, DateUtils.format(TOMORROW, DateUtils.SHORT_DATE))
            )
        ));
  }

  @Test
  void getSummaryView_withNoSatToSubmit() {
    testEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithNoSatToSubmit(applicationVersion);
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(testEiaDirection));

    var summaryDataView = eiaDirectionService.getEiaDirectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView)
        .usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(
                new SummaryKeyValue(HAVE_SUBMITTED_EIA_DIRECTION_PROMPT, "No"),
                new SummaryKeyValue(HAVE_EIA_DIRECTION_TO_SUBMIT_PROMPT, "No"),
                new SummaryKeyValue(EXPLANATION_PROMPT, WHY_NO_EIA_DIRECTION)
            )
        ));
  }

  @Test
  void getSummaryView_withAllEiaDataSet_noSatToSubmit() {
    testEiaDirection = EiaDirectionTestUtil.getEiaDirectionWithAllDataSet(applicationVersion, SAT_ID_1, SAT_REF_1);
    when(eiaDirectionRepository.findByApplicationVersion(applicationVersion))
        .thenReturn(Optional.of(testEiaDirection));

    var summaryDataView = eiaDirectionService.getEiaDirectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView)
        .usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(
                new SummaryKeyValue(HAVE_SUBMITTED_EIA_DIRECTION_PROMPT, "No"),
                new SummaryKeyValue(HAVE_EIA_DIRECTION_TO_SUBMIT_PROMPT, "No"),
                new SummaryKeyValue(EXPLANATION_PROMPT, WHY_NO_EIA_DIRECTION)
            )
        ));
  }
}