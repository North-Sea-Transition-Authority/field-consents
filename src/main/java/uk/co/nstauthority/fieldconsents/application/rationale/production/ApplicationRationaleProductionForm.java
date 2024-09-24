package uk.co.nstauthority.fieldconsents.application.rationale.production;

import java.util.Collections;
import java.util.List;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationale;
import uk.co.nstauthority.fieldconsents.application.rationale.ApplicationRationaleType;

public record ApplicationRationaleProductionForm(
    ApplicationRationaleType rationaleType,
    StringInput increaseComment,
    StringInput decreaseComment,
    StringInput extensionComment,
    StringInput otherComment,
    String productionLocationAssetKeysSelector,
    List<String> productionLocationAssetKeys,
    String hostLocationAssetKey
) {

  public ApplicationRationaleProductionForm {
    increaseComment = new StringInput("increaseComment", "why you are requesting an increase");
    decreaseComment = new StringInput("decreaseComment", "why you are requesting a decrease");
    extensionComment = new StringInput("extensionComment", "why you are requesting an extension");
    otherComment = new StringInput("otherComment", "why you have selected 'other'");
  }

  public static ApplicationRationaleProductionForm empty() {
    return new ApplicationRationaleProductionForm(
        null,
        null,
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
        null,
        null,
        null
    );

    var rationaleType = applicationRationale.getRationaleType();
    switch (rationaleType) {
      case INCREASE:
        form.increaseComment().setInputValue((applicationRationale.getComment()));
        break;
      case DECREASE:
        form.decreaseComment().setInputValue((applicationRationale.getComment()));
        break;
      case EXTENSION:
        form.extensionComment().setInputValue((applicationRationale.getComment()));
        break;
      case OTHER:
        form.otherComment().setInputValue((applicationRationale.getComment()));
        break;
      case NO_CHANGE:
      default:
        break;
    }

    return form;
  }

}
