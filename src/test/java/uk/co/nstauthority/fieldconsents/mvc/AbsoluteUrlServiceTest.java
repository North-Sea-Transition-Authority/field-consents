package uk.co.nstauthority.fieldconsents.mvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties;

@ExtendWith(MockitoExtension.class)
class AbsoluteUrlServiceTest {

  @Mock
  private ServiceConfigurationProperties serviceConfigurationProperties;

  @InjectMocks
  private AbsoluteUrlService absoluteUrlService;

  @Test
  void getAbsoluteUrl() {
    var baseUrl = "testBaseUrl";

    when(serviceConfigurationProperties.baseUrl()).thenReturn(baseUrl);

    var url = "testUrl";

    assertThat(absoluteUrlService.getAbsoluteUrl(url)).isEqualTo(baseUrl + url);
  }
}
