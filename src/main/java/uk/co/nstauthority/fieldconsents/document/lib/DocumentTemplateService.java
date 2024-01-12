package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DocumentTemplateService {

  private final DocumentTemplateRepository documentTemplateRepository;

  @Autowired
  DocumentTemplateService(DocumentTemplateRepository documentTemplateRepository) {
    this.documentTemplateRepository = documentTemplateRepository;
  }

  @Transactional
  public DocumentTemplateDto createDocumentTemplate(
      String mnemonic,
      String title,
      String description,
      String templatePath,
      int displayOrder
  ) {
    var documentTemplate = new DocumentTemplate();

    documentTemplate.setMnemonic(mnemonic);
    documentTemplate.setTitle(title);
    documentTemplate.setDescription(description);
    documentTemplate.setTemplatePath(templatePath);
    documentTemplate.setDisplayOrder(displayOrder);

    documentTemplateRepository.save(documentTemplate);

    return DocumentTemplateDto.from(documentTemplate);
  }

  public DocumentTemplateDto getDocumentTemplateDtoOrThrow(UUID documentTemplateId) {
    return DocumentTemplateDto.from(getDocumentTemplateOrThrow(documentTemplateId));
  }

  DocumentTemplate getDocumentTemplateOrThrow(UUID documentTemplateId) {
    return documentTemplateRepository.findById(documentTemplateId)
        .orElseThrow(() ->
            new DocumentTemplateNotFoundException("Unable to find document template %s".formatted(documentTemplateId))
        );
  }

  public List<DocumentTemplateDto> getDocumentTemplateDtos() {
    return documentTemplateRepository.findAll().stream()
        .map(DocumentTemplateDto::from)
        .toList();
  }

  public Optional<DocumentTemplateDto> getDocumentTemplateDtoByMnemonic(String mnemonic) {
    return documentTemplateRepository.findByMnemonic(mnemonic).map(DocumentTemplateDto::from);
  }

  public DocumentTemplateDto getDocumentTemplateDtoByMnemonicOrThrow(String mnemonic) {
    return getDocumentTemplateDtoByMnemonic(mnemonic)
        .orElseThrow(() ->
            new DocumentTemplateNotFoundException("Unable to find document template with mnemonic [%s]".formatted(mnemonic))
        );
  }

}
