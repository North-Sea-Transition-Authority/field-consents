package uk.co.nstauthority.fieldconsents.flarevent.vent.vents;

import java.util.List;
import java.util.Map;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class VentTestUtil {
  public static ApplicationVersion ventAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT);
  public static String BASE_VENTS_URL = "/applications/" + ApplicationTestUtil.APPLICATION_ID + "/vents";
  public static Integer badVentNo = 999;
  public static Integer ventNoHp = 1;
  public static Vent ventHp = new Vent(ventAppVersion, ventNoHp, VentType.HP_VENT,
      "HP DESCRIPTION", Boolean.TRUE, "HP COMMENT");
  public static VentView ventViewHp = new VentView(null,
      ventNoHp,
      BASE_VENTS_URL + "/" + ventNoHp,
      BASE_VENTS_URL + "/" + ventNoHp + "/delete",
      ventHp.getVentType().getDisplayName(),
      ventHp.getDescription(),
      ventHp.getMeteredFlag() ? "Yes" : "No",
      ventHp.getComments()
  );
  public static Vent ventHp2 = new Vent(ventAppVersion, ventNoHp, VentType.HP_VENT,
      "HP DESCRIPTION 2", Boolean.FALSE, "HP COMMENT 2");

  public static VentForm ventFormHp2 = new VentForm(ventHp2.getVentType(),
      getNewStringInput("description", "Description", ventHp2.getDescription()),
      ventHp2.getMeteredFlag(),
      getNewStringInput("commentsMeteredYes", "Comments", null),
      getNewStringInput("commentsMeteredNo", "Comments", ventHp2.getComments())
  );

  public static Vent ventLp = new Vent(ventAppVersion, 3, VentType.LP_VENT,
      "LP DESCRIPTION", Boolean.TRUE, "LP COMMENT");
  public static Vent ventOther = new Vent(ventAppVersion, 5, VentType.OTHER_VENT,
      "OTHER DESCRIPTION", Boolean.FALSE, "OTHER COMMENT");
  public static List<Vent> vents = List.of(ventHp, ventLp, ventOther);

  public static List<VentView> ventViews =
      List.of(VentView.from(ventHp, 1),
          VentView.from(ventLp, 3),
          VentView.from(ventOther, 2)
      );

  public static Map<String, String> ventTypesAsMap =
      Map.of(VentType.HP_VENT.getEnumName(), VentType.HP_VENT.getDisplayName(),
          VentType.LP_VENT.getEnumName(), VentType.LP_VENT.getDisplayName(),
          VentType.OTHER_VENT.getEnumName(), VentType.OTHER_VENT.getDisplayName()
      );

  public static StringInput getNewStringInput(String fieldName, String displayName, String inputValue) {
    StringInput stringInput = new StringInput(fieldName, displayName);
    stringInput.setInputValue(inputValue);
    return stringInput;
  }

}
