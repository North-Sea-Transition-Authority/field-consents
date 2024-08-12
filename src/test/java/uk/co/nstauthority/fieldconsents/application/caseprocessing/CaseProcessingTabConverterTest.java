package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaseProcessingTabConverterTest {

  @InjectMocks
  private CaseProcessingTabConverter caseProcessingTabConverter;

  @ParameterizedTest
  @EnumSource(CaseProcessingTab.class)
  void convert_anchor(CaseProcessingTab caseProcessingTab) {
    assertThat(caseProcessingTabConverter.convert(caseProcessingTab.getAnchor())).isEqualTo(caseProcessingTab);
  }

  @ParameterizedTest
  @EnumSource(CaseProcessingTab.class)
  void convert_names(CaseProcessingTab caseProcessingTab) {
    assertThat(caseProcessingTabConverter.convert(caseProcessingTab.name())).isEqualTo(caseProcessingTab);
  }

  @Test
  void convert_defaultTab() {
    assertThat(caseProcessingTabConverter.convert("some invalid tab")).isEqualTo(CaseProcessingTab.VIEW_APPLICATION);
  }

}