package uk.co.nstauthority.fieldconsents.document.lib;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import freemarker.template.Configuration;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

@Service
public class DocumentInstanceService {

  private final DocumentInstanceRepository documentInstanceRepository;
  private final DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService;
  private final DocumentTemplateService documentTemplateService;
  private final Configuration freemarkerConfiguration;

  @Autowired
  DocumentInstanceService(
      DocumentInstanceRepository documentInstanceRepository,
      DocumentInstanceSectionTemplateCopyingService documentInstanceSectionTemplateCopyingService,
      DocumentTemplateService documentTemplateService,
      Configuration freemarkerConfiguration
  ) {
    this.documentInstanceRepository = documentInstanceRepository;
    this.documentInstanceSectionTemplateCopyingService = documentInstanceSectionTemplateCopyingService;
    this.documentTemplateService = documentTemplateService;
    this.freemarkerConfiguration = freemarkerConfiguration;
  }

  @Transactional
  public DocumentInstanceDto createDocumentInstance(
      String itemReference,
      String itemType,
      DocumentTemplateDto documentTemplateDto
  ) {
    var documentTemplate = documentTemplateService.getDocumentTemplateOrThrow(documentTemplateDto.id());

    var documentInstance = new DocumentInstance();

    documentInstance.setItemReference(itemReference);
    documentInstance.setItemType(itemType);
    documentInstance.setDocumentTemplate(documentTemplate);

    documentInstanceRepository.save(documentInstance);

    documentInstanceSectionTemplateCopyingService.copyDocumentTemplateSectionsToDocumentInstance(documentInstance);

    return DocumentInstanceDto.from(documentInstance);
  }

  public DocumentInstanceDto getDocumentInstanceDtoOrThrow(UUID documentInstanceId) {
    return DocumentInstanceDto.from(getDocumentInstanceOrThrow(documentInstanceId));
  }

  DocumentInstance getDocumentInstanceOrThrow(UUID documentInstanceId) {
    return documentInstanceRepository.findById(documentInstanceId)
        .orElseThrow(() ->
            new DocumentInstanceNotFoundException("Unable to find document instance %s".formatted(documentInstanceId))
        );
  }

  public ByteArrayResource renderPdf(DocumentInstanceDto documentInstanceDto, Map<String, Object> templateModel) {
    var documentInstanceId = documentInstanceDto.id();

    var model = new HashMap<>(templateModel);
    model.put("documentInstanceDto", documentInstanceDto);

    try {
      var freemarkerTemplate =
          freemarkerConfiguration.getTemplate(documentInstanceDto.documentTemplateDto().templatePath());
      var documentHtml = FreeMarkerTemplateUtils.processTemplateIntoString(freemarkerTemplate, model);

      return renderPdfFromHtml(documentHtml);
    } catch (Exception exception) {
      throw new RuntimeException(
          "Exception rendering PDF for document instance: %s".formatted(documentInstanceId),
          exception
      );
    }
  }

  ByteArrayResource renderPdfFromHtml(String html) throws IOException {
    var pdfRendererBuilder = new PdfRendererBuilder();
    pdfRendererBuilder.withHtmlContent(html, "classpath://");

    try (var outputStream = new ByteArrayOutputStream()) {
      pdfRendererBuilder.toStream(outputStream);
      pdfRendererBuilder.run();

      return new ByteArrayResource(outputStream.toByteArray());
    }
  }

  @Transactional
  public void reloadDocumentInstance(DocumentInstanceDto documentInstanceDto) {
    var documentInstance = getDocumentInstanceOrThrow(documentInstanceDto.id());

    documentInstanceSectionTemplateCopyingService.reloadDocumentInstanceSectionsFromDocumentTemplate(documentInstance);
  }
}
