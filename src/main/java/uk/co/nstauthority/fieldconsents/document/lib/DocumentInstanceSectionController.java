package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.web.servlet.ModelAndView;

public interface DocumentInstanceSectionController {

  ModelAndView getAddDocumentInstanceSectionBefore(UUID documentInstanceSectionId);

  ModelAndView getAddDocumentInstanceSectionAfter(UUID documentInstanceSectionId);

  ModelAndView getAddDocumentInstanceSubsection(UUID documentInstanceSectionId);

  ModelAndView getEditDocumentInstanceSection(UUID documentInstanceSectionId);

  ModelAndView getRemoveDocumentInstanceSection(UUID documentInstanceSectionId);
}
