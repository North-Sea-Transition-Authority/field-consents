package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;

class FormattedFieldEquityPartnerTest {

  private static final String ORGANISATION_UNIT_NAME = "SHELL";
  private static final String REGISTERED_NUMBER = "123";

  @Test
  void from_validFieldEquityPartner() {
    var fieldEquityPartner = FieldEquityPartner.newBuilder()
        .organisationUnit(OrganisationUnit.newBuilder()
            .name(ORGANISATION_UNIT_NAME)
            .registeredNumber(REGISTERED_NUMBER)
            .build())
        .build();

    assertThat(FormattedFieldEquityPartner.from(fieldEquityPartner))
        .isEqualTo(new FormattedFieldEquityPartner(ORGANISATION_UNIT_NAME, REGISTERED_NUMBER));
  }

  @Test
  void from_missingOrganisationUnit() {
    var fieldEquityPartner = FieldEquityPartner.newBuilder()
        .build();

    assertThatThrownBy(() -> FormattedFieldEquityPartner.from(fieldEquityPartner))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("OrganisationUnit is null in FieldEquityPartner. Is it included in the EPA query?");
  }

  @Test
  void from_missingOrganisationName() {
    var fieldEquityPartner = FieldEquityPartner.newBuilder()
        .organisationUnit(OrganisationUnit.newBuilder()
            .organisationUnitId(1)
            .registeredNumber(REGISTERED_NUMBER)
            .build())
        .build();

    assertThatThrownBy(() -> FormattedFieldEquityPartner.from(fieldEquityPartner))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Organisation unit [%s] is missing a name".formatted(1));
  }

  @Test
  void from_missingRegisteredNumber() {
    var fieldEquityPartner = FieldEquityPartner.newBuilder()
        .organisationUnit(OrganisationUnit.newBuilder()
            .name(ORGANISATION_UNIT_NAME)
            .build())
        .build();

    assertThat(FormattedFieldEquityPartner.from(fieldEquityPartner))
        .isEqualTo(new FormattedFieldEquityPartner(
            ORGANISATION_UNIT_NAME,
            null
        ));
  }

  @ParameterizedTest
  @MethodSource("getFormattedValue_arguments")
  void getFormattedValue(FormattedFieldEquityPartner formattedFieldEquityPartner, String expectedFormattedValue) {
    assertThat(formattedFieldEquityPartner.getFormattedValue()).isEqualTo(expectedFormattedValue);
  }

  private static Stream<Arguments> getFormattedValue_arguments() {
    return Stream.of(
        arguments(
            new FormattedFieldEquityPartner(ORGANISATION_UNIT_NAME, REGISTERED_NUMBER),
            "%s (%s)".formatted(ORGANISATION_UNIT_NAME, REGISTERED_NUMBER)
        ),
        arguments(
            new FormattedFieldEquityPartner(ORGANISATION_UNIT_NAME, null),
            ORGANISATION_UNIT_NAME
        )
    );
  }

  @Test
  void compareTo_isInDefaultStringSortOrderForOrganisationName() {
    var aFormattedFieldEquityPartner = new FormattedFieldEquityPartner("a", "c");
    var bFormattedFieldEquityPartner = new FormattedFieldEquityPartner("b", "b");
    var cFormattedFieldEquityPartner = new FormattedFieldEquityPartner("c", "a");

    var sorted = Stream.of(
            aFormattedFieldEquityPartner,
            cFormattedFieldEquityPartner,
            bFormattedFieldEquityPartner
        )
        .sorted()
        .toList();

    assertThat(sorted).containsExactly(
        aFormattedFieldEquityPartner,
        bFormattedFieldEquityPartner,
        cFormattedFieldEquityPartner
    );
  }
}
