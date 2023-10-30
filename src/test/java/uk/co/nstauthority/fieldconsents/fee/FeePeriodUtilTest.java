package uk.co.nstauthority.fieldconsents.fee;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

class FeePeriodUtilTest {

  @Test
  void getTitle_nullEndDate() {
    var id = UUID.randomUUID();
    var startDate = LocalDate.now();

    var feePeriodDto = new FeePeriodDto(id, startDate, null);

    var formattedStartDate = DateUtils.format(feePeriodDto.startDate().atStartOfDay(), DateUtils.SHORT_DATE);

    assertThat(FeePeriodUtil.getTitle(feePeriodDto))
        .isEqualTo("%s and onwards".formatted(formattedStartDate));
  }

  @Test
  void getTitle_nonNullEndDate() {
    var id = UUID.randomUUID();
    var startDate = LocalDate.now();
    var endDate = startDate.plusWeeks(1);

    var feePeriodDto = new FeePeriodDto(id, startDate, endDate);

    var formattedStartDate = DateUtils.format(startDate.atStartOfDay(), DateUtils.SHORT_DATE);
    var formattedEndDate = DateUtils.format(DateUtils.atEndOfDay(endDate), DateUtils.SHORT_DATE);

    assertThat(FeePeriodUtil.getTitle(feePeriodDto))
        .isEqualTo("%s - %s".formatted(formattedStartDate, formattedEndDate));
  }
}
