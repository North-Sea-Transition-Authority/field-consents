package uk.co.nstauthority.fieldconsents.application.rationale.production;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;

public record ApplicationRationaleProductionForm(
    ApplicationRationaleType rationaleType,
    StringInput extensionComment,
    StringInput otherComment,
    String productionLocationAssetKeysSelector,
    List<String> productionLocationAssetKeys,
    String hostLocationAssetKey
) {

  public ApplicationRationaleProductionForm {
    extensionComment = new StringInput("extensionComment", "why you are requesting an extension");
    otherComment = new StringInput("otherComment", "why you have selected 'other'");
  }

  public static ApplicationRationaleProductionForm empty() {
    return new ApplicationRationaleProductionForm(
        null,
        null,
        null,
        null,
        Collections.emptyList(),
        null
    );
  }

  public static ApplicationRationaleProductionForm from(ApplicationRationale applicationRationale) {
    var form = new ApplicationRationaleProductionForm(
        applicationRationale.getRationaleType(),
        null,
        null,
        null,
        null,
        null
    );

    var rationaleType = applicationRationale.getRationaleType();
    if (ApplicationRationaleType.EXTENSION.equals(rationaleType)) {
      form.extensionComment().setInputValue(applicationRationale.getComment());
    }
    if (ApplicationRationaleType.OTHER.equals(rationaleType)) {
      form.otherComment().setInputValue(applicationRationale.getComment());
    }

    return form;
  }

}
