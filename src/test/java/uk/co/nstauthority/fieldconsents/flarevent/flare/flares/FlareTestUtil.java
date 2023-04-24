package uk.co.nstauthority.fieldconsents.flarevent.flare.flares;

import java.util.List;
import java.util.Map;
import uk.co.fivium.formlibrary.input.StringInput;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class FlareTestUtil {
  public static ApplicationVersion flareAppVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE);
  public static String BASE_FLARES_URL = "/applications/" + ApplicationTestUtil.APPLICATION_ID + "/flares";
  public static Integer badFlareNo = 999;
  public static Integer flareNoHp = 1;
  public static Flare flareHp = new Flare(flareAppVersion, flareNoHp, FlareType.HP_FLARE,
      "HP DESCRIPTION", Boolean.TRUE, "HP COMMENT");
  public static FlareView flareViewHp = new FlareView(null,
      flareNoHp,
      BASE_FLARES_URL + "/" + flareNoHp,
      BASE_FLARES_URL + "/" + flareNoHp + "/delete",
      flareHp.getFlareType().getDisplayName(),
      flareHp.getDescription(),
      flareHp.getMeteredFlag() ? "Yes" : "No",
      flareHp.getComments()
  );
  public static Flare flareHp2 = new Flare(flareAppVersion, flareNoHp, FlareType.HP_FLARE,
      "HP DESCRIPTION 2", Boolean.FALSE, "HP COMMENT 2");

  public static FlareForm flareFormHp2 = new FlareForm(flareHp2.getFlareType(),
      getNewStringInput("description", "Description", flareHp2.getDescription()),
      flareHp2.getMeteredFlag(),
      getNewStringInput("commentsMeteredYes", "Comments", null),
      getNewStringInput("commentsMeteredNo", "Comments", flareHp2.getComments())
  );

  public static Flare flareMp = new Flare(flareAppVersion, 3, FlareType.MP_FLARE,
      "MP DESCRIPTION", Boolean.FALSE, "MP COMMENT");
  public static Flare flareLp = new Flare(flareAppVersion, 5, FlareType.LP_FLARE,
      "LP DESCRIPTION", Boolean.TRUE, "LP COMMENT");
  public static Flare flareLpp = new Flare(flareAppVersion, 7, FlareType.LPP_FLARE,
      "LPP DESCRIPTION", Boolean.FALSE, "LPP COMMENT");
  public static List<Flare> flares = List.of(flareHp, flareMp, flareLp, flareLpp);

  public static List<FlareView> flareViews =
      List.of(FlareView.from(flareHp, 1), FlareView.from(flareMp, 2),
          FlareView.from(flareLp, 3), FlareView.from(flareLpp, 4)
      );

  public static Map<String, String> flareTypesAsMap =
      Map.of(FlareType.HP_FLARE.getEnumName(), FlareType.HP_FLARE.getDisplayName(),
          FlareType.MP_FLARE.getEnumName(), FlareType.MP_FLARE.getDisplayName(),
          FlareType.LP_FLARE.getEnumName(), FlareType.LP_FLARE.getDisplayName(),
          FlareType.LPP_FLARE.getEnumName(), FlareType.LPP_FLARE.getDisplayName()
      );

  public static StringInput getNewStringInput(String fieldName, String displayName, String inputValue) {
    StringInput stringInput = new StringInput(fieldName, displayName);
    stringInput.setInputValue(inputValue);
    return stringInput;
  }

}
