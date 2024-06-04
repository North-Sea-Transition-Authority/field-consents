package uk.co.nstauthority.fieldconsents.document;

import org.jsoup.safety.Safelist;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentBeanConfiguration {

  @Bean
  Safelist fieldConsentsDocumentSafelist() {
    return Safelist.basic()
        .addAttributes("p", "style")
        .addTags("s");
  }
}
