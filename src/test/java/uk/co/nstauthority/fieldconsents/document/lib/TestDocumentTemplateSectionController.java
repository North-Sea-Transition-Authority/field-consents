package uk.co.nstauthority.fieldconsents.document.lib;

import java.util.UUID;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/test/document-templates/section/{documentTemplateSectionId}")
class TestDocumentTemplateSectionController implements DocumentTemplateSectionController {

  @GetMapping("/add-before")
  @Override
  public ModelAndView getAddDocumentTemplateSectionBefore(@PathVariable UUID documentTemplateSectionId) {
    return null;
  }

  @GetMapping("/add-after")
  @Override
  public ModelAndView getAddDocumentTemplateSectionAfter(@PathVariable UUID documentTemplateSectionId) {
    return null;
  }

  @GetMapping("/add-subsection")
  @Override
  public ModelAndView getAddDocumentTemplateSubsection(@PathVariable UUID documentTemplateSectionId) {
    return null;
  }

  @GetMapping("/edit")
  @Override
  public ModelAndView getEditDocumentTemplateSection(@PathVariable UUID documentTemplateSectionId) {
    return null;
  }

  @GetMapping("/remove")
  @Override
  public ModelAndView getRemoveDocumentTemplateSection(@PathVariable UUID documentTemplateSectionId) {
    return null;
  }
}
