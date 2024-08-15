package uk.co.nstauthority.fieldconsents.licences;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;
import org.junit.jupiter.api.Test;

class LicenceJsonComparatorTest {

  @Test
  void compare_whenDifferentLicenceType_thenOrderedByLicenceType() {

    var firstLicence = LicenceJsonTestUtil.builder()
        .withLicenceType("a")
        .build();

    var secondLicence = LicenceJsonTestUtil.builder()
        .withLicenceType("b")
        .build();

    var thirdLicence = LicenceJsonTestUtil.builder()
        .withLicenceType("C")
        .build();

    var unsortedList = List.of(
        thirdLicence,
        secondLicence,
        firstLicence
    );

    var sortedList = unsortedList
        .stream()
        .sorted(new LicenceJsonComparator())
        .toList();

    assertThat(sortedList)
        .extracting(LicenceJson::licenceType)
        .containsExactly(
            firstLicence.licenceType(),
            secondLicence.licenceType(),
            thirdLicence.licenceType()
        );
  }

  @Test
  void compare_whenDifferentLicenceNumber_thenOrderedByLicenceNumber() {

    var firstLicence = LicenceJsonTestUtil.builder()
        .withLicenceNumber(1)
        .build();

    var secondLicence = LicenceJsonTestUtil.builder()
        .withLicenceNumber(2)
        .build();

    var thirdLicence = LicenceJsonTestUtil.builder()
        .withLicenceNumber(10)
        .build();

    var unsortedList = List.of(
        thirdLicence,
        secondLicence,
        firstLicence
    );

    var sortedList = unsortedList
        .stream()
        .sorted(new LicenceJsonComparator())
        .toList();

    assertThat(sortedList)
        .extracting(LicenceJson::licenceNo)
        .containsExactly(
            firstLicence.licenceNo(),
            secondLicence.licenceNo(),
            thirdLicence.licenceNo()
        );
  }

  @Test
  void compare_whenAllPropertiesNull_thenNullsFirstInList() {

    var licenceWithNullProperties = LicenceJsonTestUtil.builder()
        .withLicenceType(null)
        .withLicenceNumber(null)
        .withLicenceReference(null)
        .build();

    var licenceWithNoNullProperties = LicenceJsonTestUtil.builder().build();

    var unsortedList = List.of(licenceWithNoNullProperties, licenceWithNullProperties);

    var sortedList = unsortedList
        .stream()
        .sorted(new LicenceJsonComparator())
        .toList();

    assertThat(sortedList)
        .extracting(
            LicenceJson::licenceType,
            LicenceJson::licenceNo
        )
        .containsExactly(
            tuple(
                licenceWithNullProperties.licenceType(),
                licenceWithNullProperties.licenceNo()
            ),
            tuple(
                licenceWithNoNullProperties.licenceType(),
                licenceWithNoNullProperties.licenceNo()
            )
        );
  }
}
