package uk.co.nstauthority.fieldconsents.application.rationale.emissions;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;

public record ApplicationRationaleEmissionsForm(
    ApplicationRationaleType rationaleType,
    StringInput increaseComment,
    StringInput decreaseComment,
    String locationAssetKeysSelector,
    List<String> locationAssetKeys,
    String hostLocationAssetKey
) {

  public ApplicationRationaleEmissionsForm {
    increaseComment = new StringInput("increaseComment", "why you are requesting an increase");
    decreaseComment = new StringInput("decreaseComment", "why you are requesting a decrease");
  }

  public static ApplicationRationaleEmissionsForm empty() {
    return new ApplicationRationaleEmissionsForm(
        null,
        null,
        null,
        null,
        Collections.emptyList(),
        null
    );
  }

  public static ApplicationRationaleEmissionsForm from(ApplicationRationale applicationRationale) {
    var form = new ApplicationRationaleEmissionsForm(
        applicationRationale.getRationaleType(),
        null,
        null,
        null,
        Collections.emptyList(),
        null
    );

    if (ApplicationRationaleType.INCREASE.equals(applicationRationale.getRationaleType())) {
      form.increaseComment().setInputValue(applicationRationale.getComment());
    } else if (ApplicationRationaleType.DECREASE.equals(applicationRationale.getRationaleType())) {
      form.decreaseComment().setInputValue(applicationRationale.getComment());
    }

    return form;
  }

}
