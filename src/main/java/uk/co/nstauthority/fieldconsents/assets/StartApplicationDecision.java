package uk.co.nstauthority.fieldconsents.assets;

import java.util.List;

public record StartApplicationDecision(List<String> reasonsWhyCannotBeStarted) {

  public static StartApplicationDecision allowed() {
    return new StartApplicationDecision(List.of());
  }

  public static StartApplicationDecision notAllowed(List<String> reasons) {
    return new StartApplicationDecision(reasons);
  }

  public boolean canBeStarted() {
    return reasonsWhyCannotBeStarted.isEmpty();
  }

}
