package uk.co.nstauthority.fieldconsents.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringUtilTest {

  @ParameterizedTest
  @MethodSource("getFormatStringListArguments")
  void formatStringList(List<String> list, String expected) {
    assertThat(StringUtil.formatStringList(list)).isEqualTo(expected);
  }

  private static Stream<Arguments> getFormatStringListArguments() {
    return Stream.of(
        Arguments.of(List.of(""), ""),
        Arguments.of(List.of("One"), "One"),
        Arguments.of(List.of("One", "Two"), "One and Two"),
        Arguments.of(List.of("One", "Two", "Three"), "One, Two and Three"),
        Arguments.of(List.of("One", "Two", "Three", "Four"), "One, Two, Three and Four")
    );
  }
}
