package uk.co.nstauthority.fieldconsents.document.templatesectioncondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagService;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentInstanceLinkingService;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateDtoTestUtil;
import uk.co.nstauthority.fieldconsents.document.DocumentTemplateType;

@ExtendWith(MockitoExtension.class)
class GasWillBeInjectedConditionTest {

  @Mock
  private DocumentInstanceLinkingService documentInstanceLinkingService;

  @Mock
  private ApplicationFlagService applicationFlagService;

  @InjectMocks
  private GasWillBeInjectedCondition gasWillBeInjectedCondition;

  @Test
  void getMnemonic() {
    assertThat(gasWillBeInjectedCondition.getMnemonic()).isEqualTo("GAS_WILL_BE_INJECTED");
  }

  @Test
  void getTitle() {
    assertThat(gasWillBeInjectedCondition.getTitle()).isEqualTo("Gas will be injected");
  }

  @ParameterizedTest
  @EnumSource(DocumentTemplateType.class)
  void isApplicable(DocumentTemplateType documentTemplateType) {
    var template = DocumentTemplateDtoTestUtil.builder()
        .withMnemonic(documentTemplateType.getMnemonic())
        .build();

    assertThat(gasWillBeInjectedCondition.isApplicable(template))
        .isEqualTo(documentTemplateType == DocumentTemplateType.FIELD_PRODUCTION_CONSENT);
  }

  @Test
  void evaluate_flagValueIsNotPresent() {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED))
        .thenReturn(Optional.empty());

    assertThat(gasWillBeInjectedCondition.evaluate(documentInstanceDto)).isFalse();
  }

  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  void evaluate_flagValueIsPresent(boolean willGasBeInjected) {
    var documentInstanceDto = DocumentInstanceDtoTestUtil.builder().build();
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    when(documentInstanceLinkingService.getLatestApplicationVersionFromDocumentInstanceDto(documentInstanceDto))
        .thenReturn(applicationVersion);
    when(applicationFlagService.findFlagValue(applicationVersion, ApplicationFlagType.WILL_GAS_BE_INJECTED))
        .thenReturn(Optional.of(willGasBeInjected));

    assertThat(gasWillBeInjectedCondition.evaluate(documentInstanceDto)).isEqualTo(willGasBeInjected);
  }
}
