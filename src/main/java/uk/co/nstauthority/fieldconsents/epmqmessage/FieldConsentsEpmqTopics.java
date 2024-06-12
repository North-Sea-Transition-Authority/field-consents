package uk.co.nstauthority.fieldconsents.epmqmessage;

public enum FieldConsentsEpmqTopics {

  APPLICATIONS("field-consents-applications");

  private final String name;

  FieldConsentsEpmqTopics(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
