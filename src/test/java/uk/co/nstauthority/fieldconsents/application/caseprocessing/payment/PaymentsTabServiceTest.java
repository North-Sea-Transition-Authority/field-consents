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
import uk.co.fivium.digitalpaymentslibrary.payment.PaymentStatus;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.payment.ApplicationPaymentService;
import uk.co.nstauthority.fieldconsents.application.payment.PaymentDtoTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class PaymentsTabServiceTest {

  @Mock
  private ApplicationVersionService applicationVersionService;

  @Mock
  private ApplicationPaymentService applicationPaymentService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  @Spy
  private PaymentsTabService paymentsTabService;

  @Test
  void addPaymentsTabContentToModelAndView() {
    var application = new Application();
    var modelAndView = new ModelAndView();

    var paymentsTabPaymentSummaryViews =
        List.of(mock(PaymentsTabPaymentSummaryView.class), mock(PaymentsTabPaymentSummaryView.class));

    doReturn(paymentsTabPaymentSummaryViews)
        .when(paymentsTabService)
        .getPaymentsTabPaymentSummaryViews(application);

    paymentsTabService.addPaymentsTabContentToModelAndView(application, modelAndView);

    assertThat(modelAndView.getModel()).containsExactly(
        entry("paymentsTabPaymentSummaryViews", paymentsTabPaymentSummaryViews)
    );
  }

  @Test
  void getPaymentsTabPaymentSummaryViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var applicationVersions = List.of(
        ApplicationTestUtil.getNewApplicationVersionWithIdAndType(1, ApplicationType.PRODUCTION),
        ApplicationTestUtil.getNewApplicationVersionWithIdAndType(2, ApplicationType.PRODUCTION)
    );

    var paymentDto1CreatedByUserId = "1";
    var paymentDto2CreatedByUserId = "2";
    var paymentDto3CreatedByUserId = "3";

    var now = Instant.now();

    var paymentDto1 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto1CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now)
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    var paymentDto2 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto2CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now.minusMillis(1))
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    var paymentDto3 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto3CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now.minusMillis(2))
        .withStatus(PaymentStatus.SUCCESS)
        .build();

    var paymentDtos = List.of(paymentDto3, paymentDto1, paymentDto2);

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

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId())).thenReturn(applicationVersions);

    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(paymentDtos);

    when(energyPortalUserService.getEnergyPortalUserMap(wuaIds)).thenReturn(energyPortalUserMap);

    assertThat(paymentsTabService.getPaymentsTabPaymentSummaryViews(application)).containsExactly(
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
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var applicationVersions = List.of(
        ApplicationTestUtil.getNewApplicationVersionWithIdAndType(1, ApplicationType.PRODUCTION),
        ApplicationTestUtil.getNewApplicationVersionWithIdAndType(2, ApplicationType.PRODUCTION)
    );

    var paymentDto1CreatedByUserId = "1";
    var paymentDto2CreatedByUserId = "2";

    var now = Instant.now();

    var paymentDto1 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto1CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now)
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    var paymentDto2 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto2CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now.minusMillis(1))
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    var paymentDto3 = PaymentDtoTestUtil.builder()
        .withStatus(otherPaymentStatus)
        .build();

    var paymentDtos = List.of(paymentDto3, paymentDto1, paymentDto2);

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

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId())).thenReturn(applicationVersions);

    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(paymentDtos);

    when(energyPortalUserService.getEnergyPortalUserMap(wuaIds)).thenReturn(energyPortalUserMap);

    assertThat(paymentsTabService.getPaymentsTabPaymentSummaryViews(application)).containsExactly(
        PaymentsTabPaymentSummaryView.from(paymentDto1, ServiceUserDetail.from(energyPortalUserDto1)),
        PaymentsTabPaymentSummaryView.from(paymentDto2, ServiceUserDetail.from(energyPortalUserDto2))
    );
  }

  @Test
  void getPaymentsTabPaymentSummaryViews_webUserAccountIdNotFound() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);

    var applicationVersions = List.of(
        ApplicationTestUtil.getNewApplicationVersionWithIdAndType(1, ApplicationType.PRODUCTION),
        ApplicationTestUtil.getNewApplicationVersionWithIdAndType(2, ApplicationType.PRODUCTION)
    );

    var paymentDto1CreatedByUserId = "1";
    var paymentDto2CreatedByUserId = "2";
    var paymentDto3CreatedByUserId = "3";

    var now = Instant.now();

    var paymentDto1 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto1CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now)
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    var paymentDto2 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto2CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now.minusMillis(1))
        .withStatus(PaymentStatus.SUCCESS)
        .build();
    var paymentDto3 = PaymentDtoTestUtil.builder()
        .withCreatedByUserId(paymentDto3CreatedByUserId)
        .withGovUkPayCaptureSubmitInstant(now.minusMillis(2))
        .withStatus(PaymentStatus.SUCCESS)
        .build();

    var paymentDtos = List.of(paymentDto3, paymentDto1, paymentDto2);

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

    var energyPortalUserMap = Map.of(
        webUserAccountId1, energyPortalUserDto1,
        webUserAccountId2, energyPortalUserDto2
    );

    when(applicationVersionService.getAllApplicationVersionsByApplicationId(application.getId())).thenReturn(applicationVersions);

    when(applicationPaymentService.getPaymentDtos(applicationVersions)).thenReturn(paymentDtos);

    when(energyPortalUserService.getEnergyPortalUserMap(wuaIds)).thenReturn(energyPortalUserMap);

    assertThatThrownBy(() -> paymentsTabService.getPaymentsTabPaymentSummaryViews(application))
        .isInstanceOf(IllegalStateException.class);
  }
}
