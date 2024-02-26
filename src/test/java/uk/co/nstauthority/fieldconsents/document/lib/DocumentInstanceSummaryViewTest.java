package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

class DocumentInstanceSummaryViewTest {

  @Test
  void from() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    assertThat(DocumentInstanceSummaryView.from(documentInstanceDto, TestDocumentInstanceController.class)).isEqualTo(
        new DocumentInstanceSummaryView(
            documentInstanceDto.id(),
            documentInstanceDto.title(),
            documentInstanceDto.description(),
            ReverseRouter.route(on(TestDocumentInstanceController.class).getViewDocumentInstance(documentInstanceDto.id()))
        )
    );
  }
}
