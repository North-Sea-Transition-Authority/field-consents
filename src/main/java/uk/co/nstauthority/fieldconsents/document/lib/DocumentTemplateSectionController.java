package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.web.servlet.ModelAndView;

public interface DocumentTemplateSectionController {

  ModelAndView getAddDocumentTemplateSectionBefore(UUID documentTemplateSectionId);

  ModelAndView getAddDocumentTemplateSectionAfter(UUID documentTemplateSectionId);

  ModelAndView getAddDocumentTemplateSubsection(UUID documentTemplateSectionId);

  ModelAndView getEditDocumentTemplateSection(UUID documentTemplateSectionId);

  ModelAndView getRemoveDocumentTemplateSection(UUID documentTemplateSectionId);
}
