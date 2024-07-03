package uk.co.nstauthority.fieldconsents.assets;

import java.util.List;

public record StartApplicationDecision(
    boolean canBeStarted,
    List<String> reasonsWhyCannotBeStarted
) {

  public static StartApplicationDecision allowed() {
    return new StartApplicationDecision(true, List.of());
  }

  public static StartApplicationDecision notAllowed(List<String> reasonsWhyCannotBeStarted) {
    return new StartApplicationDecision(false, reasonsWhyCannotBeStarted);
  }

}
