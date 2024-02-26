package uk.co.nstauthority.fieldconsents.document.lib;

class DocumentSectionNumberingUtil {

  private DocumentSectionNumberingUtil() {
    throw new IllegalStateException(
        "DocumentSectionNumberingUtil is a utility class and cannot be instantiated"
    );
  }

  static String getFullNumberSectionNumberString(String parentSectionNumberString, int sectionNumber) {
    if (parentSectionNumberString == null) {
      return "%d".formatted(sectionNumber);
    }

    return "%s.%d".formatted(parentSectionNumberString, sectionNumber);
  }
}
