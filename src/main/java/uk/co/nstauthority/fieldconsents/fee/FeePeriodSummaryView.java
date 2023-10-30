package uk.co.nstauthority.fieldconsents.fee;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import javax.annotation.Nullable;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

public record FeePeriodSummaryView(
    String title,
    String formattedStartDate,
    @Nullable String formattedEndDate,
    FeePeriodStatus status,
    String viewUrl,
    @Nullable String editUrl
) {

  public static FeePeriodSummaryView from(
      FeePeriodDto feePeriodDto,
      FeePeriodStatus feePeriodStatus,
      boolean editable
  ) {
    var title = FeePeriodUtil.getTitle(feePeriodDto);

    var formattedStartDate = DateUtils.format(feePeriodDto.startDate().atStartOfDay(), DateUtils.DATE_TIME);
    var formattedEndDate = feePeriodDto.endDate() == null
        ? null
        : DateUtils.format(DateUtils.atEndOfDay(feePeriodDto.endDate()), DateUtils.DATE_TIME);

    var viewUrl = ReverseRouter.route(on(FeePeriodController.class).getViewFeePeriod(feePeriodDto.id()));
    var editUrl = !editable
        ? null
        : ReverseRouter.route(on(FeePeriodController.class).getEditFeePeriod(feePeriodDto.id(), null));

    return new FeePeriodSummaryView(
        title,
        formattedStartDate,
        formattedEndDate,
        feePeriodStatus,
        viewUrl,
        editUrl
    );
  }
}
