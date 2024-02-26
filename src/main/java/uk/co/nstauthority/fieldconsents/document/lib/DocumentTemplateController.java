package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.web.servlet.ModelAndView;

public interface DocumentTemplateController {

  ModelAndView getViewDocumentTemplate(UUID documentTemplateId);
}
