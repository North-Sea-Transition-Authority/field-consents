package uk.co.nstauthority.fieldconsents.mvc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties;

@Service
public class AbsoluteUrlService {

  private final ServiceConfigurationProperties serviceConfigurationProperties;

  @Autowired
  public AbsoluteUrlService(ServiceConfigurationProperties serviceConfigurationProperties) {
    this.serviceConfigurationProperties = serviceConfigurationProperties;
  }

  public String getAbsoluteUrl(String url) {
    return serviceConfigurationProperties.baseUrl() + url;
  }
}
