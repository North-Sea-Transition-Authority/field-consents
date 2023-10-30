package uk.co.nstauthority.fieldconsents.fee;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class FeePeriodSummaryViewTest {

  @Test
  void from_nullEndDateAndNotEditable() {
    var id = UUID.randomUUID();
    var startDate = LocalDate.now();

    var feePeriodDto = new FeePeriodDto(id, startDate, null);
    var feePeriodStatus = FeePeriodStatus.ACTIVE;
    var editable = false;

    var view = FeePeriodSummaryView.from(feePeriodDto, feePeriodStatus, editable);

    assertThat(view).isEqualTo(
        new FeePeriodSummaryView(
            FeePeriodUtil.getTitle(feePeriodDto),
            DateUtils.format(feePeriodDto.startDate().atStartOfDay(), DateUtils.DATE_TIME),
            null,
            feePeriodStatus,
            ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(feePeriodDto.id())),
            null
        )
    );
  }

  @Test
  void from_nonNullEndDate() {
    var id = UUID.randomUUID();
    var startDate = LocalDate.now();
    var endDate = LocalDate.now().plusDays(1);

    var feePeriodDto = new FeePeriodDto(id, startDate, endDate);
    var feePeriodStatus = FeePeriodStatus.ACTIVE;
    var editable = false;

    var view = FeePeriodSummaryView.from(feePeriodDto, feePeriodStatus, editable);

    assertThat(view).isEqualTo(
        new FeePeriodSummaryView(
            FeePeriodUtil.getTitle(feePeriodDto),
            DateUtils.format(feePeriodDto.startDate().atStartOfDay(), DateUtils.DATE_TIME),
            DateUtils.format(DateUtils.atEndOfDay(feePeriodDto.endDate()), DateUtils.DATE_TIME),
            feePeriodStatus,
            ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(feePeriodDto.id())),
            null
        )
    );
  }

  @Test
  void from_editable() {
    var id = UUID.randomUUID();
    var startDate = LocalDate.now();

    var feePeriodDto = new FeePeriodDto(id, startDate, null);
    var feePeriodStatus = FeePeriodStatus.PENDING;
    var editable = true;

    var view = FeePeriodSummaryView.from(feePeriodDto, feePeriodStatus, editable);

    assertThat(view).isEqualTo(
        new FeePeriodSummaryView(
            FeePeriodUtil.getTitle(feePeriodDto),
            DateUtils.format(feePeriodDto.startDate().atStartOfDay(), DateUtils.DATE_TIME),
            null,
            feePeriodStatus,
            ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(feePeriodDto.id())),
            ReverseRouter.route(on(FeePeriodController.class).getEditFeePeriod(feePeriodDto.id(), null))
        )
    );
  }
}
