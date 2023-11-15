package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;

class ApplicationContextTest {

  @Test
  void getPrimaryOperatorPrompt() {
    assertThat(ApplicationContext.newBuilder().build())
        .extracting(ApplicationContext::getPrimaryOperatorPrompt)
        .isEqualTo("Primary operator");
  }

  @Test
  void getStatusPrompt() {
    assertThat(ApplicationContext.newBuilder().build())
        .extracting(ApplicationContext::getStatusPrompt)
        .isEqualTo("Status");
  }

  @Test
  void getStartingYearPrompt() {
    assertThat(ApplicationContext.newBuilder().build())
        .extracting(ApplicationContext::getStartingYearPrompt)
        .isEqualTo("Starting year");
  }

  @Test
  void getConsentDurationPrompt() {
    assertThat(ApplicationContext.newBuilder().build())
        .extracting(ApplicationContext::getConsentDurationPrompt)
        .isEqualTo("Consent duration");
  }

  @ParameterizedTest
  @MethodSource("getAssetOperatorsPrompt_arguments")
  void getAssetOperatorsPrompt(
      AssetJson primaryAsset,
      Set<String> assetOperators,
      String expectedPrompt
  ) {
    var applicationContext = ApplicationContext.newBuilder().withPrimaryAsset(primaryAsset).withAssetOperators(assetOperators).build();
    assertThat(applicationContext.getAssetOperatorsPrompt()).isEqualTo(expectedPrompt);
  }

  private static Stream<Arguments> getAssetOperatorsPrompt_arguments() {
    return Stream.of(
        arguments(
            field1Json,
            Set.of("op1", "op2"),
            "Field operators"
        ),
        arguments(
            field1Json,
            Collections.singleton("op1"),
            "Field operator"
        ),
        arguments(
            terminal1Json,
            Set.of("op1", "op2"),
            "Facility operators"
        ),
        arguments(
            terminal1Json,
            Collections.singleton("op1"),
            "Facility operator"
        )
    );
  }

  @Test
  void getAdditionalFieldsPrompt_single() {
    assertThat(ApplicationContext.newBuilder().withAdditionalFields(Collections.singleton("f1")).build())
        .extracting(ApplicationContext::getAdditionalFieldsPrompt)
        .isEqualTo("Additional field");
  }

  @Test
  void getAdditionalFieldsPrompt_multiple() {
    assertThat(ApplicationContext.newBuilder().withAdditionalFields(Set.of("f1", "f2")).build())
        .extracting(ApplicationContext::getAdditionalFieldsPrompt)
        .isEqualTo("Additional fields");
  }

  @Test
  void getLicencesPrompt_single() {
    assertThat(ApplicationContext.newBuilder().withLicences(Collections.singleton("l1")).build())
        .extracting(ApplicationContext::getLicencesPrompt)
        .isEqualTo("Licence");
  }

  @Test
  void getLicencesPrompt_multiple() {
    assertThat(ApplicationContext.newBuilder().withLicences(Set.of("l1", "l2")).build())
        .extracting(ApplicationContext::getLicencesPrompt)
        .isEqualTo("Licences");
  }

  @Test
  void getCommaSeparatedAssetOperators_single() {
    assertThat(ApplicationContext.newBuilder().withAssetOperators(Collections.singleton("a1")).build())
        .extracting(ApplicationContext::getCommaSeparatedAssetOperators)
        .isEqualTo("a1");
  }

  @Test
  void getCommaSeparatedAssetOperators_multiple() {
    assertThat(ApplicationContext.newBuilder().withAssetOperators(Set.of("a1", "a3", "a2")).build())
        .extracting(ApplicationContext::getCommaSeparatedAssetOperators)
        .isEqualTo("a1, a2, a3");
  }

  @Test
  void getCommaSeparatedAdditionalFields_single() {
    assertThat(ApplicationContext.newBuilder().withAdditionalFields(Collections.singleton("a1")).build())
        .extracting(ApplicationContext::getCommaSeparatedAdditionalFields)
        .isEqualTo("a1");
  }

  @Test
  void getCommaSeparatedAdditionalFields_multiple() {
    assertThat(ApplicationContext.newBuilder().withAdditionalFields(Set.of("f1", "f3", "f2")).build())
        .extracting(ApplicationContext::getCommaSeparatedAdditionalFields)
        .isEqualTo("f1, f2, f3");
  }

  @Test
  void getCommaSeparatedLicences_single() {
    assertThat(ApplicationContext.newBuilder().withLicences(Collections.singleton("l1")).build())
        .extracting(ApplicationContext::getCommaSeparatedLicences)
        .isEqualTo("l1");
  }

  @Test
  void getCommaSeparatedLicences_multiple() {
    assertThat(ApplicationContext.newBuilder().withLicences(Set.of("l1", "l3", "l2")).build())
        .extracting(ApplicationContext::getCommaSeparatedLicences)
        .isEqualTo("l1, l2, l3");
  }
}
