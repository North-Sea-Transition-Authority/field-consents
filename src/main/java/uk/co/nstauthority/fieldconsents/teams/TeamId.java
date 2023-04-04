package uk.co.nstauthority.fieldconsents.teams;

import java.io.Serial;
import java.io.Serializable;

public record TeamId(Integer id) implements Serializable {

  @Serial
  private static final long serialVersionUID = -5692556481153190999L;

  public static TeamId valueOf(Integer value) {
    return new TeamId(value);
  }

  // Required for Spring converter mapping
  public static TeamId valueOf(String value) {
    return new TeamId(Integer.valueOf(value));
  }

  @Override
  public String toString() {
    return id.toString();
  }
}
