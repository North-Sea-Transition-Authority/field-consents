package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DocumentInstanceControllerHelperService {

  public List<DocumentInstanceSummaryView> getDocumentInstanceSummaryViews(
      Collection<DocumentInstanceDto> documentInstanceDtos,
      Class<? extends DocumentInstanceController> documentInstanceControllerClass
  ) {
    return documentInstanceDtos
        .stream()
        .sorted(Comparator.comparingInt(documentInstance -> documentInstance.documentTemplateDto().displayOrder()))
        .map(documentInstanceDto -> DocumentInstanceSummaryView.from(documentInstanceDto, documentInstanceControllerClass))
        .toList();
  }
}
