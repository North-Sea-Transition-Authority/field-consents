package uk.co.nstauthority.fieldconsents.application.caseprocessing.payment;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class PaymentsTabService {

  private final ApplicationVersionService applicationVersionService;
  private final ApplicationPaymentService applicationPaymentService;
  private final EnergyPortalUserService energyPortalUserService;

  @Autowired
  PaymentsTabService(
      ApplicationVersionService applicationVersionService,
      ApplicationPaymentService applicationPaymentService,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.applicationVersionService = applicationVersionService;
    this.applicationPaymentService = applicationPaymentService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public void addPaymentsTabContentToModelAndView(Application application, ModelAndView modelAndView) {
    modelAndView.addObject("paymentsTabPaymentSummaryViews", getPaymentsTabPaymentSummaryViews(application));
  }

  List<PaymentsTabPaymentSummaryView> getPaymentsTabPaymentSummaryViews(Application application) {
    var applicationVersions = applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId());

    var paymentDtos = applicationPaymentService.getPaymentDtos(applicationVersions).stream()
        .filter(paymentDto -> paymentDto.status() == PaymentStatus.SUCCESS)
        .toList();

    var wuaIds = paymentDtos.stream()
        .map(PaymentDto::createdByUserId)
        .map(WebUserAccountId::valueOf)
        .collect(Collectors.toSet());

    var energyPortalUserByWuaId = energyPortalUserService.getEnergyPortalUserMap(wuaIds)
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> Long.toString(entry.getKey().id()),
            Map.Entry::getValue
        ));

    return paymentDtos.stream()
        .sorted(Comparator.comparing(PaymentDto::govUkPayCaptureSubmitInstant).reversed())
        .map(paymentDto ->
            PaymentsTabPaymentSummaryView.from(
                paymentDto,
                ServiceUserDetail.from(
                    Optional.ofNullable(energyPortalUserByWuaId.get(paymentDto.createdByUserId()))
                        .orElseThrow(() ->
                            new IllegalStateException("No EnergyPortalUserDto found for user ID %s"
                                .formatted(paymentDto.createdByUserId()))
                        )
                )
            )
        )
        .toList();
  }
}
