package uk.co.nstauthority.fieldconsents.energyportal;

import java.io.Serial;
import java.io.Serializable;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

public record WebUserAccountId(long id) implements Serializable {

  @Serial
  private static final long serialVersionUID = -1836128817324202878L;

  public static WebUserAccountId valueOf(String value) {
    return new WebUserAccountId(Long.parseLong(value));
  }

  public static WebUserAccountId from(ServiceUserDetail user) {
    return new WebUserAccountId(user.wuaId());
  }

  public int toInt() {
    return ((Long) id).intValue();
  }

  @Override
  public String toString() {
    return String.valueOf(id);
  }
}
