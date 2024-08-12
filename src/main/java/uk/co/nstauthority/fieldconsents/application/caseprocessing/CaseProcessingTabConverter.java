package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import java.util.EnumSet;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CaseProcessingTabConverter implements Converter<String, CaseProcessingTab> {

  private static final CaseProcessingTab DEFAULT_TAB = CaseProcessingTab.VIEW_APPLICATION;

  @Override
  public CaseProcessingTab convert(String tab) {
    return EnumSet
        .allOf(CaseProcessingTab.class).stream()
        .filter(caseProcessingTab -> caseProcessingTab.getAnchor().equals(tab) || caseProcessingTab.name().equals(tab))
        .findFirst()
        .orElse(DEFAULT_TAB);
  }
}
