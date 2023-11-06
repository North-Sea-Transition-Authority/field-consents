package uk.co.nstauthority.fieldconsents.fee;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.digitalpaymentslibrary.fee.FeeLineDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodDto;
import uk.co.fivium.digitalpaymentslibrary.fee.FeePeriodService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class FieldConsentsFeePeriodService {

  private final FeePeriodService feePeriodService;
  private final Clock clock;

  @Autowired
  FieldConsentsFeePeriodService(FeePeriodService feePeriodService, Clock clock) {
    this.feePeriodService = feePeriodService;
    this.clock = clock;
  }

  List<FeePeriodSummaryView> getFeePeriodSummaryViews() {
    return feePeriodService.getFeePeriodDtos().stream()
        .sorted(Comparator.comparing(FeePeriodDto::startDate))
        .map(feePeriodDto ->
            FeePeriodSummaryView.from(
                feePeriodDto,
                getFeePeriodStatus(feePeriodDto),
                isFeePeriodEditable(feePeriodDto)
            )
        )
        .toList();
  }

  FeePeriodStatus getFeePeriodStatus(FeePeriodDto feePeriodDto) {
    var now = LocalDate.now(clock);

    if (feePeriodDto.startDate().isAfter(now)) {
      return FeePeriodStatus.PENDING;
    }

    var endDate = feePeriodDto.endDate();
    if (endDate != null && endDate.isBefore(now)) {
      return FeePeriodStatus.COMPLETE;
    }

    return FeePeriodStatus.ACTIVE;
  }

  List<FeeLineView> getFeeLineViews(List<FeeLineDto> feeLineDtos) {
    return feeLineDtos.stream()
        .sorted(Comparator.comparing(feeLineDto -> FeeLineMnemonic.from(feeLineDto.mnemonic())))
        .map(FeeLineView::from)
        .toList();
  }

  void createFeePeriod(FeePeriodForm form, ServiceUserDetail user) {
    var startDate = form.getStartDateInput().getAsLocalDate()
        .orElseThrow(() -> new IllegalStateException("Error parsing start date"));

    var latestFeePeriodFeeLineDtos = feePeriodService.getLatestFeePeriodFeeLineDtos();

    var copiedFeeLineDtos = copyFeeLineDtosWithAmountsFromForm(latestFeePeriodFeeLineDtos, form);

    feePeriodService.createFeePeriod(
        startDate,
        copiedFeeLineDtos,
        user.wuaId().toString()
    );
  }

  boolean isFeePeriodEditable(FeePeriodDto feePeriodDto) {
    return feePeriodDto.startDate().isAfter(LocalDate.now(clock));
  }

  void editFeePeriod(FeePeriodDto feePeriodDto, FeePeriodForm form, ServiceUserDetail user) {
    var startDate = form.getStartDateInput().getAsLocalDate()
        .orElseThrow(() -> new IllegalStateException("Error parsing start date"));

    var feeLineDtos = feePeriodService.getFeeLineDtosByFeePeriodId(feePeriodDto.id());
    var copiedFeeLineDtos = copyFeeLineDtosWithAmountsFromForm(feeLineDtos, form);

    feePeriodService.editFeePeriod(
        feePeriodDto,
        startDate,
        copiedFeeLineDtos,
        user.wuaId().toString()
    );
  }

  List<FeeLineDto> copyFeeLineDtosWithAmountsFromForm(
      List<FeeLineDto> feeLineDtos,
      FeePeriodForm form
  ) {
    return feeLineDtos.stream()
        .map(feeLineDto ->
            new FeeLineDto(
                feeLineDto.mnemonic(),
                feeLineDto.title(),
                Optional.ofNullable(form.getFeeLineAmountsByMnemonic().get(feeLineDto.mnemonic()))
                    .map(Double::parseDouble)
                    .map(amount -> (int) amount.doubleValue() * 100)
                    .orElseThrow(() -> new IllegalStateException(
                        "Form does not contain amount for mnemonic %s".formatted(feeLineDto.mnemonic()))
                    )
            )
        )
        .toList();
  }

  FeePeriodForm getPrefilledFeePeriodForm(
      FeePeriodDto feePeriodDto,
      List<FeeLineDto> feeLineDtos
  ) {
    var form = new FeePeriodForm();

    form.getStartDateInput().setDate(feePeriodDto.startDate());

    form.setFeeLineAmountsByMnemonic(feeLineDtos.stream()
        .collect(Collectors.toMap(
            FeeLineDto::mnemonic,
            feeLineDto -> String.format("%.2f", (double) feeLineDto.amountPence() / 100)
        )));

    return form;
  }
}
