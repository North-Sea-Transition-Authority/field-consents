package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConsentFileUsageTest {

  @Test
  void generatedConsentDocumentFrom() {
    var consent = ConsentTestUtil.newBuilder().build();

    assertThat(ConsentFileUsage.generatedConsentDocumentFrom(consent)).isEqualTo(
        new ConsentFileUsage(
            consent.getId().toString(),
            ConsentFileUsage.USAGE_TYPE,
            "generated-consent-document"
        )
    );
  }

  @Test
  void supportingConsentDocumentFrom() {
    var consent = ConsentTestUtil.newBuilder().build();

    assertThat(ConsentFileUsage.supportingConsentDocumentFrom(consent)).isEqualTo(
        new ConsentFileUsage(
            consent.getId().toString(),
            ConsentFileUsage.USAGE_TYPE,
            "supporting-consent-document"
        )
    );
  }
}
