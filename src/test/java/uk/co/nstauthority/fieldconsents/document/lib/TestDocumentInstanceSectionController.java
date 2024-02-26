package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/test/document-instances/section/{documentInstanceSectionId}")
class TestDocumentInstanceSectionController implements DocumentInstanceSectionController {

  @GetMapping("/add-before")
  @Override
  public ModelAndView getAddDocumentInstanceSectionBefore(@PathVariable UUID documentInstanceSectionId) {
    return null;
  }

  @GetMapping("/add-after")
  @Override
  public ModelAndView getAddDocumentInstanceSectionAfter(@PathVariable UUID documentInstanceSectionId) {
    return null;
  }

  @GetMapping("/add-subsection")
  @Override
  public ModelAndView getAddDocumentInstanceSubsection(@PathVariable UUID documentInstanceSectionId) {
    return null;
  }

  @GetMapping("/edit")
  @Override
  public ModelAndView getEditDocumentInstanceSection(@PathVariable UUID documentInstanceSectionId) {
    return null;
  }

  @GetMapping("/remove")
  @Override
  public ModelAndView getRemoveDocumentInstanceSection(@PathVariable UUID documentInstanceSectionId) {
    return null;
  }
}
