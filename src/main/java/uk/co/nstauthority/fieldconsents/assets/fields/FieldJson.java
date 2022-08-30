package uk.co.nstauthority.fieldconsents.assets.fields;

import javax.validation.constraints.NotNull;
import uk.co.nstauthority.fieldconsents.fds.searchselector.SearchSelectable;

public record FieldJson(
    @NotNull Integer fieldId,
    @NotNull String fieldName
) implements SearchSelectable {

  @Override
  public String getSelectionId() {
    return fieldId.toString();
  }

  @Override
  public String getSelectionText() {
    return fieldName;
  }
}
