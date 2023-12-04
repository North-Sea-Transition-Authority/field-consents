package uk.co.nstauthority.fieldconsents.document.lib;

public class DocumentTemplateSectionNumberingUtil {

  private DocumentTemplateSectionNumberingUtil() {
    throw new IllegalStateException(
        "DocumentTemplateSectionNumberingUtil is a utility class and cannot be instantiated"
    );
  }

  public static String getFullNumberSectionNumberString(String parentSectionNumberString, int sectionNumber) {
    if (parentSectionNumberString == null) {
      return "%d".formatted(sectionNumber);
    }

    return "%s.%d".formatted(parentSectionNumberString, sectionNumber);
  }
}
