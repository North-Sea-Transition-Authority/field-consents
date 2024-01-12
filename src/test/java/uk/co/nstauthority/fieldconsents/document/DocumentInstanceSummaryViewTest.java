package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentInstanceSummaryViewTest {

  @Test
  void from() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    assertThat(DocumentInstanceSummaryView.from(documentInstanceDto)).isEqualTo(
        new DocumentInstanceSummaryView(
            documentInstanceDto.title(),
            documentInstanceDto.description(),
            ReverseRouter.route(on(DocumentInstanceController.class).getViewDocumentInstance(documentInstanceDto.id()))
        ));
  }
}
