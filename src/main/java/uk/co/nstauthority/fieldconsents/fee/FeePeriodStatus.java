package uk.co.nstauthority.fieldconsents.fee;

public enum FeePeriodStatus {

  PENDING("Pending", "govuk-tag--blue"),
  ACTIVE("Active", "govuk-tag--turquoise"),
  COMPLETE("Complete", "govuk-tag--grey");

  private final String tagText;
  private final String tagClass;

  FeePeriodStatus(String tagText, String tagClass) {
    this.tagText = tagText;
    this.tagClass = tagClass;
  }

  public String getTagText() {
    return tagText;
  }

  public String getTagClass() {
    return tagClass;
  }
}
