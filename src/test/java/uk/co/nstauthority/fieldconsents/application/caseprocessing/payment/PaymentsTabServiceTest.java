package uk.co.nstauthority.fieldconsents.application.caseprocessing.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentDto;
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class PaymentsTabServiceTest {

  @Mock
  private ApplicationPaymentService applicationPaymentService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  @Spy
  private PaymentsTabService paymentsTabService;

  @Test
  void addPaymentsTabContentToModelAndView() {
    var applicationVersion = new ApplicationVersion();
    var modelAndView = new ModelAndView();

    var paymentsTabPaymentSummaryViews =
        List.of(mock(PaymentsTabPaymentSummaryView.class), mock(PaymentsTabPaymentSummaryView.class));

    doReturn(paymentsTabPaymentSummaryViews)
        .when(paymentsTabService)
        .getPaymentsTabPaymentSummaryViews(applicationVersion);

    paymentsTabService.addPaymentsTabContentToModelAndView(applicationVersion, modelAndView);

    assertThat(modelAndView.getModel()).containsExactly(
        entry("paymentsTabPaymentSummaryViews", paymentsTabPaymentSummaryViews)
    );
  }

  @Test
  void getPaymentsTabPaymentSummaryViews() {
    var applicationVersion = new ApplicationVersion();

    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDto3 = mock(PaymentDto.class);

    var paymentDto1CreatedByUserId = "1";
    var paymentDto2CreatedByUserId = "2";
    var paymentDto3CreatedByUserId = "3";

    var webUserAccountId1 = WebUserAccountId.valueOf(paymentDto1CreatedByUserId);
    var webUserAccountId2 = WebUserAccountId.valueOf(paymentDto2CreatedByUserId);
    var webUserAccountId3 = WebUserAccountId.valueOf(paymentDto3CreatedByUserId);

    var wuaIds = Set.of(
        webUserAccountId1,
        webUserAccountId2,
        webUserAccountId3
    );

    var energyPortalUserDto1 = mock(EnergyPortalUserDto.class);
    var energyPortalUserDto2 = mock(EnergyPortalUserDto.class);
    var energyPortalUserDto3 = mock(EnergyPortalUserDto.class);

    var energyPortalUserMap = Map.of(
        webUserAccountId1, energyPortalUserDto1,
        webUserAccountId2, energyPortalUserDto2,
        webUserAccountId3, energyPortalUserDto3
    );

    var paymentDtos = List.of(paymentDto3, paymentDto1, paymentDto2);

    var now = Instant.now();

    when(paymentDto1.status()).thenReturn(PaymentStatus.SUCCESS);
    when(paymentDto2.status()).thenReturn(PaymentStatus.SUCCESS);
    when(paymentDto3.status()).thenReturn(PaymentStatus.SUCCESS);

    when(paymentDto1.createdByUserId()).thenReturn(paymentDto1CreatedByUserId);
    when(paymentDto2.createdByUserId()).thenReturn(paymentDto2CreatedByUserId);
    when(paymentDto3.createdByUserId()).thenReturn(paymentDto3CreatedByUserId);

    when(energyPortalUserService.getEnergyPortalUserMap(wuaIds)).thenReturn(energyPortalUserMap);

    when(paymentDto1.govUkPayCaptureSubmitInstant()).thenReturn(now);
    when(paymentDto2.govUkPayCaptureSubmitInstant()).thenReturn(now.minusMillis(1));
    when(paymentDto3.govUkPayCaptureSubmitInstant()).thenReturn(now.minusMillis(2));

    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(paymentDtos);

    assertThat(paymentsTabService.getPaymentsTabPaymentSummaryViews(applicationVersion)).containsExactly(
        PaymentsTabPaymentSummaryView.from(paymentDto1, ServiceUserDetail.from(energyPortalUserDto1)),
        PaymentsTabPaymentSummaryView.from(paymentDto2, ServiceUserDetail.from(energyPortalUserDto2)),
        PaymentsTabPaymentSummaryView.from(paymentDto3, ServiceUserDetail.from(energyPortalUserDto3))
    );
  }

  @ParameterizedTest
  @EnumSource(value = PaymentStatus.class, names = "SUCCESS", mode = EnumSource.Mode.EXCLUDE)
  void getPaymentsTabPaymentSummaryViews_paymentWithStatusOtherThanSuccessNotIncluded(
      PaymentStatus otherPaymentStatus
  ) {
    var applicationVersion = new ApplicationVersion();

    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDto3 = mock(PaymentDto.class);

    var paymentDto1CreatedByUserId = "1";
    var paymentDto2CreatedByUserId = "2";

    var webUserAccountId1 = WebUserAccountId.valueOf(paymentDto1CreatedByUserId);
    var webUserAccountId2 = WebUserAccountId.valueOf(paymentDto2CreatedByUserId);

    var wuaIds = Set.of(
        webUserAccountId1,
        webUserAccountId2
    );

    var energyPortalUserDto1 = mock(EnergyPortalUserDto.class);
    var energyPortalUserDto2 = mock(EnergyPortalUserDto.class);

    var energyPortalUserMap = Map.of(
        webUserAccountId1, energyPortalUserDto1,
        webUserAccountId2, energyPortalUserDto2
    );

    var paymentDtos = List.of(paymentDto3, paymentDto1, paymentDto2);

    var now = Instant.now();

    when(paymentDto1.status()).thenReturn(PaymentStatus.SUCCESS);
    when(paymentDto2.status()).thenReturn(PaymentStatus.SUCCESS);
    when(paymentDto3.status()).thenReturn(otherPaymentStatus);

    when(paymentDto1.createdByUserId()).thenReturn(paymentDto1CreatedByUserId);
    when(paymentDto2.createdByUserId()).thenReturn(paymentDto2CreatedByUserId);

    when(energyPortalUserService.getEnergyPortalUserMap(wuaIds)).thenReturn(energyPortalUserMap);

    when(paymentDto1.govUkPayCaptureSubmitInstant()).thenReturn(now);
    when(paymentDto2.govUkPayCaptureSubmitInstant()).thenReturn(now.minusMillis(1));

    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(paymentDtos);

    assertThat(paymentsTabService.getPaymentsTabPaymentSummaryViews(applicationVersion)).containsExactly(
        PaymentsTabPaymentSummaryView.from(paymentDto1, ServiceUserDetail.from(energyPortalUserDto1)),
        PaymentsTabPaymentSummaryView.from(paymentDto2, ServiceUserDetail.from(energyPortalUserDto2))
    );
  }

  @Test
  void getPaymentsTabPaymentSummaryViews_webUserAccountIdNotFound() {
    var applicationVersion = new ApplicationVersion();

    var paymentDto1 = mock(PaymentDto.class);
    var paymentDto2 = mock(PaymentDto.class);
    var paymentDto3 = mock(PaymentDto.class);

    var paymentDto1CreatedByUserId = "1";
    var paymentDto2CreatedByUserId = "2";
    var paymentDto3CreatedByUserId = "3";

    var webUserAccountId1 = WebUserAccountId.valueOf(paymentDto1CreatedByUserId);
    var webUserAccountId2 = WebUserAccountId.valueOf(paymentDto2CreatedByUserId);
    var webUserAccountId3 = WebUserAccountId.valueOf(paymentDto3CreatedByUserId);

    var wuaIds = Set.of(
        webUserAccountId1,
        webUserAccountId2,
        webUserAccountId3
    );

    var energyPortalUserDto1 = mock(EnergyPortalUserDto.class);
    var energyPortalUserDto3 = mock(EnergyPortalUserDto.class);

    var energyPortalUserMap = Map.of(
        webUserAccountId1, energyPortalUserDto1,
        webUserAccountId3, energyPortalUserDto3
    );

    var paymentDtos = List.of(paymentDto3, paymentDto1, paymentDto2);

    var now = Instant.now();

    when(paymentDto1.status()).thenReturn(PaymentStatus.SUCCESS);
    when(paymentDto2.status()).thenReturn(PaymentStatus.SUCCESS);
    when(paymentDto3.status()).thenReturn(PaymentStatus.SUCCESS);

    when(paymentDto1.createdByUserId()).thenReturn(paymentDto1CreatedByUserId);
    when(paymentDto2.createdByUserId()).thenReturn(paymentDto2CreatedByUserId);
    when(paymentDto3.createdByUserId()).thenReturn(paymentDto3CreatedByUserId);

    when(energyPortalUserService.getEnergyPortalUserMap(wuaIds)).thenReturn(energyPortalUserMap);

    when(paymentDto1.govUkPayCaptureSubmitInstant()).thenReturn(now);
    when(paymentDto2.govUkPayCaptureSubmitInstant()).thenReturn(now.minusMillis(1));
    when(paymentDto3.govUkPayCaptureSubmitInstant()).thenReturn(now.minusMillis(2));

    when(applicationPaymentService.getPaymentDtos(applicationVersion)).thenReturn(paymentDtos);

    assertThatThrownBy(() -> paymentsTabService.getPaymentsTabPaymentSummaryViews(applicationVersion))
        .isInstanceOf(IllegalStateException.class);
  }
}
