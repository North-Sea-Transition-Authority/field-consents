package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/test/document-templates")
class TestDocumentTemplateController implements DocumentTemplateController {

  @GetMapping("/{documentTemplateId}")
  @Override
  public ModelAndView getViewDocumentTemplate(@PathVariable UUID documentTemplateId) {
    return null;
  }
}
