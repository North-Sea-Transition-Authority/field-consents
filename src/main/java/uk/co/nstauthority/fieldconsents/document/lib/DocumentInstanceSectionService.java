package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceSectionService {

  private final DocumentInstanceSectionRepository documentInstanceSectionRepository;

  @Autowired
  DocumentInstanceSectionService(DocumentInstanceSectionRepository documentInstanceSectionRepository) {
    this.documentInstanceSectionRepository = documentInstanceSectionRepository;
  }

  DocumentInstanceSectionDto getDocumentInstanceSectionDto(
      DocumentInstanceSection documentInstanceSection,
      List<DocumentInstanceSection> allDocumentInstanceSections
  ) {
    var childrenDtos = allDocumentInstanceSections.stream()
        .filter(section -> section.getParent() != null
            && section.getParent().getId().equals(documentInstanceSection.getId()))
        .map(child -> getDocumentInstanceSectionDto(child, allDocumentInstanceSections))
        .toList();

    return DocumentInstanceSectionDto.from(documentInstanceSection, childrenDtos);
  }

  public List<DocumentInstanceSectionDto> getTopLevelDocumentInstanceSectionDtos(
      DocumentInstanceDto documentInstanceDto
  ) {
    var allDocumentInstanceSections =
        documentInstanceSectionRepository.findAllByDocumentInstanceId(documentInstanceDto.id());

    return allDocumentInstanceSections.stream()
        .filter(documentInstanceSection -> documentInstanceSection.getParent() == null)
        .map(documentInstanceSection ->
            getDocumentInstanceSectionDto(
                documentInstanceSection,
                allDocumentInstanceSections
            )
        )
        .toList();
  }
}
