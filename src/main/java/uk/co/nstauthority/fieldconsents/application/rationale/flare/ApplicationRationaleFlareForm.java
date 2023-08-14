package uk.co.nstauthority.fieldconsents.application.rationale.flare;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;

public record ApplicationRationaleFlareForm(
    ApplicationRationaleType rationaleType,
    StringInput increaseComment,
    String flaringLocationAssetKeysSelector,
    List<String> flaringLocationAssetKeys,
    String hostLocationAssetKey
) {

  public ApplicationRationaleFlareForm {
    increaseComment = new StringInput("increaseComment", "why you are requesting an increase");
  }

  public static ApplicationRationaleFlareForm empty() {
    return new ApplicationRationaleFlareForm(
        null,
        null,
        null,
        Collections.emptyList(),
        null
    );
  }

  public static ApplicationRationaleFlareForm from(ApplicationRationale applicationRationale) {
    var form = new ApplicationRationaleFlareForm(
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
