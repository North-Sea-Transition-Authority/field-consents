package uk.co.nstauthority.fieldconsents.document;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentInstanceService;
import uk.co.nstauthority.fieldconsents.document.lib.DocumentTemplateService;

@ExtendWith(MockitoExtension.class)
class FieldConsentsDocumentInstanceServiceTest {

  private static final String APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE = "APPLICATION";

  @Mock
  private DocumentInstanceService documentInstanceService;

  @Mock
  private DocumentTemplateService documentTemplateService;

  @Mock
  private FieldConsentsDocumentInstanceSectionService fieldConsentsDocumentInstanceSectionService;

  @InjectMocks
  private FieldConsentsDocumentInstanceService fieldConsentsDocumentInstanceService;

  @ParameterizedTest
  @MethodSource("createDocumentInstancesForApplication_arguments")
  void createDocumentInstancesForApplication(ApplicationType applicationType, DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var application = new Application(1);
    application.setType(applicationType);

    when(documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(documentTemplateType.name())).thenReturn(documentTemplateDto);
    when(documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(application.getId().toString(), APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE, documentTemplateDto))
        .thenReturn(Optional.empty());

    fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(documentInstanceService).createDocumentInstance(
        application.getId().toString(),
        APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
        documentTemplateDto.title(),
        documentTemplateType.getDocumentInstanceDescription(),
        documentTemplateDto
    );
  }

  @ParameterizedTest
  @MethodSource("createDocumentInstancesForApplication_arguments")
  void createDocumentInstancesForApplication_documentInstancesAlreadyExist(ApplicationType applicationType, DocumentTemplateType documentTemplateType) {
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var existingDocumentInstance = DocumentInstanceDtoTestUtil.builder().build();
    var application = new Application(1);
    application.setType(applicationType);

    when(documentTemplateService.getDocumentTemplateDtoByMnemonicOrThrow(documentTemplateType.name())).thenReturn(documentTemplateDto);
    when(documentInstanceService.getDocumentInstanceDtoByItemReferenceAndItemTypeAndDocumentTemplateDto(application.getId().toString(), APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE, documentTemplateDto))
        .thenReturn(Optional.of(existingDocumentInstance));

    fieldConsentsDocumentInstanceService.createDocumentInstancesForApplication(application);

    verify(documentInstanceService, never()).createDocumentInstance(any(), any(), any(), any(), any());
  }

  private static Stream<Arguments> createDocumentInstancesForApplication_arguments() {
    return Stream.of(
        arguments(ApplicationType.FLARE, DocumentTemplateType.FIELD_FLARE_CONSENT),
        arguments(ApplicationType.PRODUCTION, DocumentTemplateType.FIELD_PRODUCTION_CONSENT),
        arguments(ApplicationType.VENT, DocumentTemplateType.FIELD_VENT_CONSENT)
    );
  }

  @Test
  void createDocumentInstance() {
    var application = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION).getApplication();
    var documentTemplateDto = DocumentTemplateDtoTestUtil.builder().build();
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();

    var documentTemplateType = DocumentTemplateType.FIELD_PRODUCTION_CONSENT;
    var itemReference = application.getId().toString();

    when(documentInstanceService.createDocumentInstance(
        itemReference,
        APPLICATION_DOCUMENT_INSTANCE_ITEM_TYPE,
        documentTemplateDto.title(),
        documentTemplateType.getDocumentInstanceDescription(),
        documentTemplateDto
    )).thenReturn(documentInstanceDto);

    assertThat(fieldConsentsDocumentInstanceService.createDocumentInstance(application, documentTemplateDto, documentTemplateType))
        .isEqualTo(documentInstanceDto);
  }

  @Test
  void getDocumentInstanceSummaryViews() {
    var applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
    var application = applicationVersion.getApplication();
    var itemReference = application.getId().toString();

    var documentTemplateDto1 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(1).build();
    var documentInstanceDto1 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto1).build();

    var documentTemplateDto2 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(2).build();
    var documentInstanceDto2 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto2).build();

    var documentTemplateDto3 = DocumentTemplateDtoTestUtil.builder().withDisplayOrder(3).build();
    var documentInstanceDto3 = DocumentInstanceDtoTestUtil.builder().withDocumentTemplate(documentTemplateDto3).build();

    when(documentInstanceService.getDocumentInstanceDtosByItemReference(itemReference))
        .thenReturn(List.of(documentInstanceDto2, documentInstanceDto1, documentInstanceDto3));

    assertThat(fieldConsentsDocumentInstanceService.getDocumentInstanceSummaryViews(application))
        .containsExactly(
            DocumentInstanceSummaryView.from(documentInstanceDto1),
            DocumentInstanceSummaryView.from(documentInstanceDto2),
            DocumentInstanceSummaryView.from(documentInstanceDto3)
        );
  }

  @Test
  void renderPdf() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var pdfRenderingOptions = PdfRenderingOptions.newBuilder().build();

    var byteArrayResource = new ByteArrayResource(new byte[] {1, 2, 3});

    Map<String, Object> expectedTemplateModel = Map.of(
        "documentInstanceSectionSummaryViews",
        fieldConsentsDocumentInstanceSectionService.getDocumentInstanceSectionSummaryViews(documentInstanceDto),
        "previewWatermark",
        pdfRenderingOptions.previewWatermark()
    );

    when(documentInstanceService.renderPdf(documentInstanceDto, expectedTemplateModel))
        .thenReturn(byteArrayResource);

    assertThat(fieldConsentsDocumentInstanceService.renderPdf(documentInstanceDto, pdfRenderingOptions)).isEqualTo(byteArrayResource);
  }
}
