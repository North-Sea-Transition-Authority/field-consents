package uk.co.nstauthority.fieldconsents.production.gasinjection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;
import uk.co.nstauthority.fieldconsents.util.BooleanUtil;

@ExtendWith(MockitoExtension.class)
class GasInjectionServiceTest {

  @Mock
  private ApplicationFlagService applicationFlagService;

  @InjectMocks
  private GasInjectionService gasInjectionService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.PRODUCTION);
  }

  @Test
  void getGasInjectionForm_noExistingFlag() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED))
        .thenReturn(Optional.empty());

    var gasInjectionForm = gasInjectionService.getGasInjectionForm(applicationVersion);

    assertThat(gasInjectionForm)
        .usingRecursiveComparison()
        .isEqualTo(GasInjectionTestUtil.gasInjectionFormStub);
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getGasInjectionForm_flagExistWithValue(Boolean willGasBeInjected) {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED))
        .thenReturn(Optional.of(willGasBeInjected));

    var gasInjectionForm = gasInjectionService.getGasInjectionForm(applicationVersion);

    assertThat(gasInjectionForm)
        .usingRecursiveComparison()
        .isEqualTo(GasInjectionTestUtil.getGasInjectionForm(willGasBeInjected));
  }

  @Test
  void getGasInjectionSummaryDataView_noExistingFlag() {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED))
        .thenReturn(Optional.empty());

    var summaryDataView = gasInjectionService.getGasInjectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView).usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(new SummaryKeyValue(ApplicationFlagType.WILL_GAS_BE_INJECTED.getDisplayName(), "")
            )));

  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void getGasInjectionSummaryDataView_flagExistWithValue(Boolean willGasBeInjected) {
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED))
        .thenReturn(Optional.of(willGasBeInjected));

    var summaryDataView = gasInjectionService.getGasInjectionSummaryDataView(applicationVersion);

    assertThat(summaryDataView).usingRecursiveComparison()
        .isEqualTo(new SummaryDataView(
            List.of(new SummaryKeyValue(ApplicationFlagType.WILL_GAS_BE_INJECTED.getDisplayName(),
                BooleanUtil.yesNoFromBoolean(willGasBeInjected))
            )));

  }
}