package uk.co.nstauthority.fieldconsents.document.lib;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class DocumentTemplateSectionNumberingUtilTest {

  @ParameterizedTest
  @MethodSource("getFullNumberSectionNumberStringArguments")
  void getFullNumberSectionNumberString(String parentSectionNumberString, int sectionNumber, String expectedResult) {
    assertThat(
        DocumentTemplateSectionNumberingUtil.getFullNumberSectionNumberString(
            parentSectionNumberString,
            sectionNumber
        )
    ).isEqualTo(expectedResult);
  }

  private static Stream<Arguments> getFullNumberSectionNumberStringArguments() {
    return Stream.of(
        Arguments.of(null, 1, "1"),
        Arguments.of(null, 2, "2"),
        Arguments.of(null, 3, "3"),
        Arguments.of("1", 1, "1.1"),
        Arguments.of("1", 2, "1.2"),
        Arguments.of("1", 3, "1.3"),
        Arguments.of("1.1", 1, "1.1.1"),
        Arguments.of("1.1", 2, "1.1.2"),
        Arguments.of("1.1", 3, "1.1.3")
    );
  }
}
