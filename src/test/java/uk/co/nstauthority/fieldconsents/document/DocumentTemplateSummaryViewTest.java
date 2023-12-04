package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentTemplateSummaryViewTest {

  @Test
  void from() {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();

    assertThat(DocumentTemplateSummaryView.from(documentTemplateDto)).isEqualTo(
        new DocumentTemplateSummaryView(
            documentTemplateDto.title(),
            documentTemplateDto.description(),
            ReverseRouter.route(on(DocumentTemplateController.class).getViewDocumentTemplate(documentTemplateDto.id()))
        )
    );
  }
}
