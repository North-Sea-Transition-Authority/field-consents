package uk.co.nstauthority.fieldconsents.document.lib;

import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class FreeMarkerTemplateRenderingService {

  private final Configuration freemarkerConfiguration;

  @Autowired
  FreeMarkerTemplateRenderingService(Configuration freemarkerConfiguration) {
    this.freemarkerConfiguration = freemarkerConfiguration;
  }

  public String renderTemplate(String name, Object model) throws Exception {
    var template = freemarkerConfiguration.getTemplate(name);
    return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
  }
}
