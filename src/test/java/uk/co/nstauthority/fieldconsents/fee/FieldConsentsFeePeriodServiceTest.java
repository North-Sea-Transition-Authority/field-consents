package uk.co.nstauthority.fieldconsents.fee;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class FieldConsentsFeePeriodServiceTest {

  @Mock
  private FeePeriodService feePeriodService;

  private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

  private FieldConsentsFeePeriodService fieldConsentsFeePeriodService;

  @BeforeEach
  void setUp() {
    fieldConsentsFeePeriodService = spy(new FieldConsentsFeePeriodService(feePeriodService, clock));
  }

  @Test
  void getFeePeriodSummaryViews() {
    var now = LocalDate.now();
    var feePeriodDto1 = new FeePeriodDto(null, now.minusDays(2), now.minusDays(1));
    var feePeriodDto2 = new FeePeriodDto(null, now, now.plusDays(1));
    var feePeriodDto3 = new FeePeriodDto(null, now.plusDays(2), null);
    var feePeriodDtos = List.of(feePeriodDto2, feePeriodDto3, feePeriodDto1);

    doReturn(FeePeriodStatus.COMPLETE)
        .when(fieldConsentsFeePeriodService)
        .getFeePeriodStatus(feePeriodDto1);
    doReturn(false).when(fieldConsentsFeePeriodService).isFeePeriodEditable(feePeriodDto1);

    doReturn(FeePeriodStatus.ACTIVE)
        .when(fieldConsentsFeePeriodService)
        .getFeePeriodStatus(feePeriodDto2);
    doReturn(false).when(fieldConsentsFeePeriodService).isFeePeriodEditable(feePeriodDto2);

    doReturn(FeePeriodStatus.PENDING)
        .when(fieldConsentsFeePeriodService)
        .getFeePeriodStatus(feePeriodDto3);
    doReturn(true).when(fieldConsentsFeePeriodService).isFeePeriodEditable(feePeriodDto3);

    when(feePeriodService.getFeePeriodDtos()).thenReturn(feePeriodDtos);

    var feePeriodSummaryViews = fieldConsentsFeePeriodService.getFeePeriodSummaryViews();

    assertThat(feePeriodSummaryViews).containsExactly(
        FeePeriodSummaryView.from(feePeriodDto1, FeePeriodStatus.COMPLETE, false),
        FeePeriodSummaryView.from(feePeriodDto2, FeePeriodStatus.ACTIVE, false),
        FeePeriodSummaryView.from(feePeriodDto3, FeePeriodStatus.PENDING, true)
    );
  }

  @Test
  void getFeePeriodStatus_startDateAfterToday() {
    var feePeriodDto = new FeePeriodDto(null, LocalDate.now().plusDays(1), null);

    var feePeriodStatus = fieldConsentsFeePeriodService.getFeePeriodStatus(feePeriodDto);

    assertThat(feePeriodStatus).isEqualTo(FeePeriodStatus.PENDING);
  }

  @Test
  void getFeePeriodStatus_startAndEndDateInPast() {
    var now = LocalDate.now();
    var feePeriodDto = new FeePeriodDto(null, now.minusDays(2), now.minusDays(1));

    var feePeriodStatus = fieldConsentsFeePeriodService.getFeePeriodStatus(feePeriodDto);

    assertThat(feePeriodStatus).isEqualTo(FeePeriodStatus.COMPLETE);
  }

  @Test
  void getFeePeriodStatus_startDateToday() {
    var feePeriodDto = new FeePeriodDto(null, LocalDate.now(), null);

    var feePeriodStatus = fieldConsentsFeePeriodService.getFeePeriodStatus(feePeriodDto);

    assertThat(feePeriodStatus).isEqualTo(FeePeriodStatus.ACTIVE);
  }

  @Test
  void getFeeLineViews() {
    var feeLineDtos = List.of(
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
        new FeeLineDto("TERMINAL/FLARE/SHORT_TERM/NEW_CONSENT", "Facility Flare Short Term Consent New Consent", 930 * 100),
        new FeeLineDto("TERMINAL/FLARE/SHORT_TERM/REVISION", "Facility Flare Short Term Consent Revision", 930 * 100),
        new FeeLineDto("TERMINAL/FLARE/ANNUAL/NEW_CONSENT", "Facility Flare Annual Consent New Consent", 930 * 100),
        new FeeLineDto("TERMINAL/FLARE/ANNUAL/REVISION", "Facility Flare Annual Consent Revision", 930 * 100),
        new FeeLineDto("TERMINAL/VENT/SHORT_TERM/NEW_CONSENT", "Facility Vent Short Term Consent New Consent", 930 * 100),
        new FeeLineDto("TERMINAL/VENT/SHORT_TERM/REVISION", "Facility Vent Short Term Consent Revision", 930 * 100),
        new FeeLineDto("TERMINAL/VENT/ANNUAL/NEW_CONSENT", "Facility Vent Annual Consent New Consent", 930 * 100),
        new FeeLineDto("TERMINAL/VENT/ANNUAL/REVISION", "Facility Vent Annual Consent Revision", 930 * 100)
    );

    var feeLineViews = fieldConsentsFeePeriodService.getFeeLineViews(feeLineDtos);

    assertThat(feeLineViews).containsExactly(
        new FeeLineView(FeeLineMnemonic.from("FIELD/PRODUCTION/SHORT_TERM/NEW_CONSENT"), "1180.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/PRODUCTION/SHORT_TERM/REVISION"), "1180.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/PRODUCTION/ANNUAL/NEW_CONSENT"), "1180.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/PRODUCTION/ANNUAL/REVISION"), "1180.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/PRODUCTION/LONG_TERM/NEW_CONSENT"), "1180.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/PRODUCTION/LONG_TERM/REVISION"), "1180.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/FLARE/SHORT_TERM/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/FLARE/SHORT_TERM/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/FLARE/ANNUAL/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/FLARE/ANNUAL/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/VENT/SHORT_TERM/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/VENT/SHORT_TERM/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/VENT/ANNUAL/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("FIELD/VENT/ANNUAL/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/FLARE/SHORT_TERM/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/FLARE/SHORT_TERM/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/FLARE/ANNUAL/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/FLARE/ANNUAL/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/VENT/SHORT_TERM/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/VENT/SHORT_TERM/REVISION"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/VENT/ANNUAL/NEW_CONSENT"), "930.00"),
        new FeeLineView(FeeLineMnemonic.from("TERMINAL/VENT/ANNUAL/REVISION"), "930.00")
    );
  }

  @Test
  void createFeePeriod() {
    var form = new FeePeriodForm();

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);

    var user = ServiceUserDetailTestUtil.Builder().build();

    var latestFeePeriodFeeLineDtos = List.of(new FeeLineDto(null, null, 100));
    var copiedFeeLineDtos = List.of(new FeeLineDto(null, null, 100));

    when(feePeriodService.getLatestFeePeriodFeeLineDtos()).thenReturn(latestFeePeriodFeeLineDtos);
    doReturn(copiedFeeLineDtos)
        .when(fieldConsentsFeePeriodService)
        .copyFeeLineDtosWithAmountsFromForm(latestFeePeriodFeeLineDtos, form);

    fieldConsentsFeePeriodService.createFeePeriod(form, user);

    verify(feePeriodService).createFeePeriod(startDate, copiedFeeLineDtos, user.wuaId().toString());
  }

  @Test
  void isFeePeriodEditable_startDateInPast() {
    var feePeriodDto = new FeePeriodDto(null, LocalDate.now().minusDays(1), null);

    var editable = fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto);

    assertThat(editable).isFalse();
  }

  @Test
  void isFeePeriodEditable_startDateToday() {
    var feePeriodDto = new FeePeriodDto(null, LocalDate.now(), null);

    var editable = fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto);

    assertThat(editable).isFalse();
  }

  @Test
  void isFeePeriodEditable_startDateInFuture() {
    var feePeriodDto = new FeePeriodDto(null, LocalDate.now().plusDays(1), null);

    var editable = fieldConsentsFeePeriodService.isFeePeriodEditable(feePeriodDto);

    assertThat(editable).isTrue();
  }

  @Test
  void editFeePeriod() {
    var feePeriodDto = new FeePeriodDto(UUID.randomUUID(), null, null);

    var form = new FeePeriodForm();

    var startDate = LocalDate.of(2023, 10, 18);
    form.getStartDateInput().setDate(startDate);

    var user = ServiceUserDetailTestUtil.Builder().build();

    var feeLineDtos = List.of(new FeeLineDto(null, null, 100));
    var copiedFeeLineDtos = List.of(new FeeLineDto(null, null, 100));

    when(feePeriodService.getFeeLineDtosByFeePeriodId(feePeriodDto.id())).thenReturn(feeLineDtos);
    doReturn(copiedFeeLineDtos)
        .when(fieldConsentsFeePeriodService)
        .copyFeeLineDtosWithAmountsFromForm(feeLineDtos, form);

    fieldConsentsFeePeriodService.editFeePeriod(feePeriodDto, form, user);

    verify(feePeriodService)
        .editFeePeriod(feePeriodDto, startDate, copiedFeeLineDtos, user.wuaId().toString());
  }

  @Test
  void copyFeeLineDtosWithAmountsFromForm_formMissingDtoMnemonic() {
    var feeLineDtos = List.of(
        new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100),
        new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200)
    );

    var form = new FeePeriodForm();

    form.setFeeLineAmountsByMnemonic(Map.of(
        "testFeeLine1Mnemonic", "3"
    ));

    assertThatThrownBy(
        () -> fieldConsentsFeePeriodService.copyFeeLineDtosWithAmountsFromForm(feeLineDtos, form)
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void copyFeeLineDtosWithAmountsFromForm() {
    var feeLineDtos = List.of(
        new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100),
        new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200)
    );

    var form = new FeePeriodForm();

    form.setFeeLineAmountsByMnemonic(Map.of(
        "testFeeLine1Mnemonic", "3",
        "testFeeLine2Mnemonic", "4"
    ));

    var copiedFeeLineDtos
        = fieldConsentsFeePeriodService.copyFeeLineDtosWithAmountsFromForm(feeLineDtos, form);

    assertThat(copiedFeeLineDtos).containsExactly(
        new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 300),
        new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 400)
    );
  }

  @Test
  void getPrefilledFeePeriodForm() {
    var now = LocalDate.now();

    var feePeriodDto = new FeePeriodDto(null, now, null);
    var feeLineDtos = List.of(
        new FeeLineDto("testFeeLine1Mnemonic", "testFeeLine1Title", 100),
        new FeeLineDto("testFeeLine2Mnemonic", "testFeeLine2Title", 200)
    );

    var form = fieldConsentsFeePeriodService.getPrefilledFeePeriodForm(feePeriodDto, feeLineDtos);

    assertThat(form.getStartDateInput().getAsLocalDate())
        .isPresent()
        .get()
        .isEqualTo(now);

    assertThat(form.getFeeLineAmountsByMnemonic()).containsExactly(
        entry("testFeeLine1Mnemonic", "1.00"),
        entry("testFeeLine2Mnemonic", "2.00")
    );
  }
}
