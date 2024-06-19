package uk.co.nstauthority.fieldconsents.energyportal.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.LogCorrelationId;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties;
import uk.co.nstauthority.fieldconsents.correlationid.CorrelationIdUtil;

@ExtendWith(MockitoExtension.class)
class EnergyPortalApiWrapperTest {

  @Mock
  private ServiceBrandingConfigurationProperties serviceBrandingConfigurationProperties;

  @InjectMocks
  private EnergyPortalApiWrapper energyPortalApiWrapper;

  private UUID correlationId;

  @BeforeEach
  void setUp() {
    correlationId = UUID.randomUUID();
    CorrelationIdUtil.setCorrelationIdOnMdc(correlationId.toString());
  }

  @Test
  void makeRequest_verifyLogCorrelationId() {
    var logCorrelationId = energyPortalApiWrapper.makeRequest(this::returnLogCorrelationId);

    assertThat(logCorrelationId).isEqualTo(correlationId.toString());
  }

  @Test
  void makeRequest_verifyRequestPurpose() {
    var requestPurpose = energyPortalApiWrapper.makeRequest(this::returnRequestPurpose);

    assertThat(requestPurpose).isEqualTo("%s: %s.%s".formatted(
        serviceBrandingConfigurationProperties.mnemonic(),
        this.getClass().getName(),
        "makeRequest_verifyRequestPurpose"
    ));
  }

  private String returnLogCorrelationId(LogCorrelationId logCorrelationId, RequestPurpose requestPurpose) {
    return logCorrelationId.id();
  }

  private String returnRequestPurpose(LogCorrelationId logCorrelationId, RequestPurpose requestPurpose) {
    return requestPurpose.purpose();
  }
}
