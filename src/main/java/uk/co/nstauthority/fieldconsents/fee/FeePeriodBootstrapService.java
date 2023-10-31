package uk.co.nstauthority.fieldconsents.fee;

import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;

@Service
public class FeePeriodBootstrapService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FeePeriodBootstrapService.class);

  private final FeePeriodService feePeriodService;

  @Autowired
  FeePeriodBootstrapService(FeePeriodService feePeriodService) {
    this.feePeriodService = feePeriodService;
  }

  @EventListener(ApplicationReadyEvent.class)
  void onApplicationReadyEvent() {
    if (!feePeriodService.getFeePeriodDtos().isEmpty()) {
      LOGGER.info("Found existing fee periods, not creating initial fee period");
      return;
    }

    LOGGER.info("Creating initial fee period");

    var feeLineDtos = List.of(
        new FeeLineDto("FIELD/PRODUCTION/SHORT_TERM/NEW_CONSENT", "Field Production Short Term Consent New Consent",
            1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/SHORT_TERM/REVISION", "Field Production Short Term Consent Revision",
            1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/ANNUAL/NEW_CONSENT", "Field Production Annual Consent New Consent",
            1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/ANNUAL/REVISION", "Field Production Annual Consent Revision", 1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/LONG_TERM/NEW_CONSENT", "Field Production Long Term Consent New Consent",
            1180 * 100),
        new FeeLineDto("FIELD/PRODUCTION/LONG_TERM/REVISION", "Field Production Long Term Consent Revision",
            1180 * 100),
        new FeeLineDto("FIELD/FLARE/SHORT_TERM/NEW_CONSENT", "Field Flare Short Term Consent New Consent",
            930 * 100),
        new FeeLineDto("FIELD/FLARE/SHORT_TERM/REVISION", "Field Flare Short Term Consent Revision", 930 * 100),
        new FeeLineDto("FIELD/FLARE/ANNUAL/NEW_CONSENT", "Field Flare Annual Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/FLARE/ANNUAL/REVISION", "Field Flare Annual Consent Revision", 930 * 100),
        new FeeLineDto("FIELD/VENT/SHORT_TERM/NEW_CONSENT", "Field Vent Short Term Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/VENT/SHORT_TERM/REVISION", "Field Vent Short Term Consent Revision", 930 * 100),
        new FeeLineDto("FIELD/VENT/ANNUAL/NEW_CONSENT", "Field Vent Annual Consent New Consent", 930 * 100),
        new FeeLineDto("FIELD/VENT/ANNUAL/REVISION", "Field Vent Annual Consent Revision", 930 * 100),
        new FeeLineDto("TERMINAL/FLARE/SHORT_TERM/NEW_CONSENT", "Facility Flare Short Term Consent New Consent",
            390 * 100),
        new FeeLineDto("TERMINAL/FLARE/SHORT_TERM/REVISION", "Facility Flare Short Term Consent Revision", 0),
        new FeeLineDto("TERMINAL/FLARE/ANNUAL/NEW_CONSENT", "Facility Flare Annual Consent New Consent", 390 * 100),
        new FeeLineDto("TERMINAL/FLARE/ANNUAL/REVISION", "Facility Flare Annual Consent Revision", 0),
        new FeeLineDto("TERMINAL/VENT/SHORT_TERM/NEW_CONSENT", "Facility Vent Short Term Consent New Consent",
            390 * 100),
        new FeeLineDto("TERMINAL/VENT/SHORT_TERM/REVISION", "Facility Vent Short Term Consent Revision", 0),
        new FeeLineDto("TERMINAL/VENT/ANNUAL/NEW_CONSENT", "Facility Vent Annual Consent New Consent", 390 * 100),
        new FeeLineDto("TERMINAL/VENT/ANNUAL/REVISION", "Facility Vent Annual Consent Revision", 0)
    );

    feePeriodService.createFeePeriod(LocalDate.of(2023, 4, 1), feeLineDtos, null);
  }
}
