package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.web.servlet.ModelAndView;

public interface DocumentInstanceController {

  ModelAndView getViewDocumentInstance(UUID documentInstanceId);
}
