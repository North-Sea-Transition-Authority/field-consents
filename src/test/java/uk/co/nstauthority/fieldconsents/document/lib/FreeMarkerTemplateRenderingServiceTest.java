package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import freemarker.template.Configuration;
import freemarker.template.Template;
import java.io.StringWriter;
import java.io.Writer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FreeMarkerTemplateRenderingServiceTest {

  @Mock
  private Configuration freemarkerConfiguration;

  @InjectMocks
  private FreeMarkerTemplateRenderingService freeMarkerTemplateRenderingService;

  @Test
  void renderTemplate() throws Exception {
    var name = "test/name.ftl";
    var model = new Object();

    var freemarkerTemplate = mock(Template.class);
    var html = "<html></html>";

    when(freemarkerConfiguration.getTemplate(name)).thenReturn(freemarkerTemplate);

    doAnswer(invocation -> {
      var writer = invocation.getArgument(1, Writer.class);
      writer.write(html);
      return null;
    })
        .when(freemarkerTemplate)
        .process(eq(model), any(StringWriter.class));

    assertThat(freeMarkerTemplateRenderingService.renderTemplate(name, model)).isEqualTo(html);
  }
}
