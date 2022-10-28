package uk.co.nstauthority.fieldconsents.util.enumutil;

/**
 * Interface which allows implementing classes to expose their human readable representation consistently.
 */
public interface Displayable {

  String getDisplayName();

  default int getDisplayOrder() {
    return 0;
  }

  String getEnumName();
}