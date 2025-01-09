package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.document;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import uk.co.fivium.fileuploadlibrary.core.UploadedFileTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.document.instance.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.template.DocumentTemplateDtoTestUtil;

class ConsentDocumentComparatorsTest {

  @Test
  void documentInstanceDto() {
    var first = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(DocumentTemplateDtoTestUtil.builder().withDisplayOrder(1).build())
        .build();

    var second = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(DocumentTemplateDtoTestUtil.builder().withDisplayOrder(2).build())
        .build();

    var third = DocumentInstanceDtoTestUtil.builder()
        .withDocumentTemplate(DocumentTemplateDtoTestUtil.builder().withDisplayOrder(3).build())
        .build();

    var documentInstanceDtos = Stream.of(second, first, third)
        .sorted(ConsentDocumentComparators.documentInstanceDto())
        .toList();

    assertThat(documentInstanceDtos).containsExactly(first, second, third);
  }

  @Test
  void supportingUploadedFile() {
    var first = UploadedFileTestUtil.newBuilder().withName("a").build();
    var second = UploadedFileTestUtil.newBuilder().withName("B").build();
    var third = UploadedFileTestUtil.newBuilder().withName("c").build();

    var uploadedFiles = Stream.of(second, first, third).sorted(ConsentDocumentComparators.supportingUploadedFile()).toList();

    assertThat(uploadedFiles).containsExactly(first, second, third);
  }
}
