package uk.co.nstauthority.fieldconsents.fee;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;

@ExtendWith(MockitoExtension.class)
class FeePeriodBootstrapServiceTest {

  @Mock
  private FeePeriodService feePeriodService;

  @InjectMocks
  private FeePeriodBootstrapService feePeriodBootstrapService;

  @Test
  void onApplicationReadyEvent_existingFeePeriodDtos() {
    when(feePeriodService.getFeePeriodDtos())
        .thenReturn(List.of(new FeePeriodDto(UUID.randomUUID(), LocalDate.now(), null)));

    feePeriodBootstrapService.onApplicationReadyEvent();

    verify(feePeriodService, never()).createFeePeriod(any(), any(), any());
  }

  @Test
  void onApplicationReadyEvent_noExistingFeePeriodDtos() {
    when(feePeriodService.getFeePeriodDtos()).thenReturn(List.of());

    feePeriodBootstrapService.onApplicationReadyEvent();

    var expectedFeeLineDtos = List.of(
        new FeeLineDto("FIELD/PRODUCTION/SHORT_TERM/NEW_CONSENT", "Field Production Short Term Consent New Consent", 1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/SHORT_TERM/REVISION", "Field Production Short Term Consent Revision", 1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/ANNUAL/NEW_CONSENT", "Field Production Annual Consent New Consent", 1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/ANNUAL/REVISION", "Field Production Annual Consent Revision", 1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/LONG_TERM/NEW_CONSENT", "Field Production Long Term Consent New Consent", 1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/LONG_TERM/REVISION", "Field Production Long Term Consent Revision", 1180 * 100),
        new FeeLineDto("FIELD/FLARE/SHORT_TERM/NEW_CONSENT", "Field Flare Short Term Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/FLARE/SHORT_TERM/REVISION", "Field Flare Short Term Consent Revision", 930 * 100),
        new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/FLARE/ANNUAL/REVISION", "Field Flare Annual Consent Revision", 930 * 100),
        new FeeLineDto("FIELD/VENT/SHORT_TERM/NEW_CONSENT", "Field Vent Short Term Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/VENT/SHORT_TERM/REVISION", "Field Vent Short Term Consent Revision", 930 * 100),
        new FeeLineDto("FIELD/VENT/ANNUAL/NEW_CONSENT", "Field Vent Annual Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/VENT/ANNUAL/REVISION", "Field Vent Annual Consent Revision", 930 * 100),
        new FeeLineDto("TERMINAL/FLARE/SHORT_TERM/NEW_CONSENT", "Facility Flare Short Term Consent New Consent", 390 * 100),
        new FeeLineDto("TERMINAL/FLARE/SHORT_TERM/REVISION", "Facility Flare Short Term Consent Revision", 0),
        new FeeLineDto("TERMINAL/FLARE/ANNUAL/NEW_CONSENT", "Facility Flare Annual Consent New Consent", 390 * 100),
        new FeeLineDto("TERMINAL/FLARE/ANNUAL/REVISION", "Facility Flare Annual Consent Revision", 0),
        new FeeLineDto("TERMINAL/VENT/SHORT_TERM/NEW_CONSENT", "Facility Vent Short Term Consent New Consent", 390 * 100),
        new FeeLineDto("TERMINAL/VENT/SHORT_TERM/REVISION", "Facility Vent Short Term Consent Revision", 0),
        new FeeLineDto("TERMINAL/VENT/ANNUAL/NEW_CONSENT", "Facility Vent Annual Consent New Consent", 390 * 100),
        new FeeLineDto("TERMINAL/VENT/ANNUAL/REVISION", "Facility Vent Annual Consent Revision", 0)
    );

    verify(feePeriodService).createFeePeriod(LocalDate.of(2023, 4, 1), expectedFeeLineDtos, null);
  }
}
