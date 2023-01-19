package uk.co.nstauthority.fieldconsents.assets;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_2;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_3;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_2;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_3;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_1;

import java.util.List;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;

public class ApplicationAssetTestUtil {

  public static String BASE_ASSETS_URL = "/applications/" + ApplicationTestUtil.APPLICATION_ID + "/additional-assets";
  public static final ApplicationAsset fieldAsset1 = new ApplicationAsset(
      1,
      ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT),
      FIELD_ID_1,
      FIELD_NAME_1,
      null,
      null,
      AssetRole.PRIMARY,
      1,
      PRIMARY_OPERATOR_OU_ID,
      CACHED_PRIMARY_OPERATOR_NAME
  );

  static final ApplicationAsset fieldAsset2 = new ApplicationAsset(
      2,
      ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT),
      FIELD_ID_2,
      FIELD_NAME_2,
      null,
      null,
      AssetRole.SECONDARY,
      2,
      PRIMARY_OPERATOR_OU_ID_2,
      CACHED_PRIMARY_OPERATOR_NAME_2
  );

  static final ApplicationAsset fieldAsset3 = new ApplicationAsset(
      3,
      ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT),
      FIELD_ID_3,
      FIELD_NAME_3,
      null,
      null,
      AssetRole.SECONDARY,
      3,
      PRIMARY_OPERATOR_OU_ID_3,
      CACHED_PRIMARY_OPERATOR_NAME_3
  );

  public static final ApplicationAsset terminalAsset1 = new ApplicationAsset(
      1,
      ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.VENT),
      null,
      null,
      TERMINAL_ID_1,
      TERMINAL_NAME_1,
      AssetRole.PRIMARY,
      1,
      PRIMARY_OPERATOR_OU_ID,
      CACHED_PRIMARY_OPERATOR_NAME
  );

  public static List<AssetView> assetViews =
      List.of(AssetView.from(fieldAsset1, 1),
          AssetView.from(fieldAsset2, 2),
          AssetView.from(fieldAsset3, 3)
      );

  public static AssetView assetView = new AssetView(
      1,
      fieldAsset1.getAssetNo(),
      fieldAsset1.getCachedFieldName(),
      fieldAsset1.getCachedAssetOperatorName(),
      BASE_ASSETS_URL + "/" + fieldAsset1.getAssetNo() + "/delete"
  );

  public static List<ApplicationAsset> assets = List.of(fieldAsset1, fieldAsset2, fieldAsset3);

}
