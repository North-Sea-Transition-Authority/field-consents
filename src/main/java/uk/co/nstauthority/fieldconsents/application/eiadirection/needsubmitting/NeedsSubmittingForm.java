package uk.co.nstauthority.fieldconsents.application.eiadirection.needsubmitting;

import java.util.Optional;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.fivium.formlibrary.input.ThreeFieldDateInput;
import uk.co.nstauthority.fieldconsents.application.eiadirection.EiaDirection;

public record NeedsSubmittingForm(
    Boolean haveEiaDirectionToSubmit,
    ThreeFieldDateInput latestDateToBeSubmitted,
    StringInput whyNoEiaDirection
) {

  public NeedsSubmittingForm {
    latestDateToBeSubmitted = new ThreeFieldDateInput("latestDateToBeSubmitted", "submission date");
    whyNoEiaDirection = new StringInput("whyNoEiaDirection", "an explanation");
  }

  public static NeedsSubmittingForm from(EiaDirection eiaDirection) {
    var form = new NeedsSubmittingForm(eiaDirection.getHaveEiaDirectionToSubmit(), null, null);
    Optional.ofNullable(eiaDirection.getLatestDateToBeSubmitted()).ifPresent(form.latestDateToBeSubmitted::setDate);
    form.whyNoEiaDirection().setInputValue(eiaDirection.getWhyNoEiaDirection());

    return form;
  }

  public static NeedsSubmittingForm empty() {
    return new NeedsSubmittingForm(null, null, null);
  }

}
