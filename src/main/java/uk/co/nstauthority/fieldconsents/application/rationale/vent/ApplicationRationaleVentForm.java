package uk.co.nstauthority.fieldconsents.application.rationale.vent;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;

public record ApplicationRationaleVentForm(
    ApplicationRationaleType rationaleType,
    StringInput increaseComment,
    String ventingLocationAssetKeysSelector,
    List<String> ventingLocationAssetKeys,
    String hostLocationAssetKey
)  {

  public ApplicationRationaleVentForm {
    increaseComment = new StringInput("increaseComment", "why you are requesting an increase");
  }

  public static ApplicationRationaleVentForm empty() {
    return new ApplicationRationaleVentForm(
        null,
        null,
        null,
        Collections.emptyList(),
        null
    );
  }

  public static ApplicationRationaleVentForm from(ApplicationRationale applicationRationale) {
    var form = new ApplicationRationaleVentForm(
        applicationRationale.getRationaleType(),
        null,
        null,
        null,
        null
    );

    form.increaseComment().setInputValue(applicationRationale.getComment());

    return form;
  }

}
