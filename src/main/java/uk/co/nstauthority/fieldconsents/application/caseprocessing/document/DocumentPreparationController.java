package uk.co.nstauthority.fieldconsents.application.caseprocessing.document;

import java.util.Collections;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.CaseProcessingActionItem;
import uk.co.nstauthority.fieldconsents.authorisation.ActionEndPoint;

@Controller
@RequestMapping("/applications/{applicationId}/document-preparation")
@ActionEndPoint(CaseProcessingActionItem.DOCUMENT_PREPARATION)
public class DocumentPreparationController {

  @GetMapping
  public ModelAndView viewDocumentInstances(@PathVariable Integer applicationId) {
    return new ModelAndView("fcs/application/document/documentPreparation")
        .addObject("pageTitle", "Document preparation")
        .addObject("documentInstanceViews", Collections.emptyList());
  }

}
