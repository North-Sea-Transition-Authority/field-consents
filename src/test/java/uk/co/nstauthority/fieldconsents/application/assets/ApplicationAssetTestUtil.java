package uk.co.nstauthority.fieldconsents.application.assets;

import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_1;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_2;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.CACHED_PRIMARY_OPERATOR_NAME_3;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_1;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_2;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.PRIMARY_OPERATOR_OU_ID_3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_ID_3;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_2;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.FIELD_NAME_3;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_ID_2;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_1;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_2;

import java.util.List;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.licences.LicenceTestUtil;

public class ApplicationAssetTestUtil {

  public static String BASE_ASSETS_URL = "/applications/" + ApplicationTestUtil.APPLICATION_ID + "/additional-assets";
  public static final ApplicationAsset fieldAsset1 = new ApplicationAsset(
      1,
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT),
      AssetType.FIELD,
      FIELD_ID_1,
      FIELD_NAME_1,
      AssetRole.PRIMARY,
      null,
      PRIMARY_OPERATOR_OU_ID_1,
      CACHED_PRIMARY_OPERATOR_NAME_1
  );

  public static final ApplicationAssetLicence fieldAsset1Licence1 =
      new ApplicationAssetLicence(
          fieldAsset1.getApplicationVersion(),
          fieldAsset1,
          LicenceTestUtil.LICENCE_ID_1,
          LicenceTestUtil.LICENCE_REF_1
      );

  public static final ApplicationAssetLicence fieldAsset1Licence2 =
      new ApplicationAssetLicence(
          fieldAsset1.getApplicationVersion(),
          fieldAsset1,
          LicenceTestUtil.LICENCE_ID_2,
          LicenceTestUtil.LICENCE_REF_2
      );

  public static final List<ApplicationAssetLicence> fieldAsset1Licences =
      List.of(fieldAsset1Licence1, fieldAsset1Licence2);

  public static final ApplicationAsset fieldAsset2 = new ApplicationAsset(
      2,
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT),
      AssetType.FIELD,
      FIELD_ID_2,
      FIELD_NAME_2,
      AssetRole.SECONDARY,
      2,
      PRIMARY_OPERATOR_OU_ID_2,
      CACHED_PRIMARY_OPERATOR_NAME_2
  );

  public static final ApplicationAssetLicence fieldAsset2Licence1 =
      new ApplicationAssetLicence(
          fieldAsset2.getApplicationVersion(),
          fieldAsset2,
          LicenceTestUtil.LICENCE_ID_1,
          LicenceTestUtil.LICENCE_REF_1
      );

  public static final ApplicationAssetLicence fieldAsset2Licence2 =
      new ApplicationAssetLicence(
          fieldAsset2.getApplicationVersion(),
          fieldAsset2,
          LicenceTestUtil.LICENCE_ID_2,
          LicenceTestUtil.LICENCE_REF_2
      );

  public static final List<ApplicationAssetLicence> fieldAsset2Licences =
      List.of(fieldAsset2Licence1, fieldAsset2Licence2);

  public static final ApplicationAsset fieldAsset3 = new ApplicationAsset(
      3,
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT),
      AssetType.FIELD,
      FIELD_ID_3,
      FIELD_NAME_3,
      AssetRole.SECONDARY,
      3,
      PRIMARY_OPERATOR_OU_ID_3,
      CACHED_PRIMARY_OPERATOR_NAME_3
  );

  public static final ApplicationAssetLicence fieldAsset3Licence1 =
      new ApplicationAssetLicence(
          fieldAsset3.getApplicationVersion(),
          fieldAsset3,
          LicenceTestUtil.LICENCE_ID_1,
          LicenceTestUtil.LICENCE_REF_1
      );

  public static final ApplicationAssetLicence fieldAsset3Licence2 =
      new ApplicationAssetLicence(
          fieldAsset3.getApplicationVersion(),
          fieldAsset3,
          LicenceTestUtil.LICENCE_ID_2,
          LicenceTestUtil.LICENCE_REF_2
      );

  public static final List<ApplicationAssetLicence> fieldAsset3Licences =
      List.of(fieldAsset3Licence1, fieldAsset3Licence2);

  public static final ApplicationAsset terminalAsset1 = new ApplicationAsset(
      1,
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT),
      AssetType.TERMINAL,
      TERMINAL_ID_1,
      TERMINAL_NAME_1,
      AssetRole.PRIMARY,
      1,
      PRIMARY_OPERATOR_OU_ID_1,
      CACHED_PRIMARY_OPERATOR_NAME_1
  );

  public static final ApplicationAsset terminalAsset2 = new ApplicationAsset(
      2,
      ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.FLARE),
      AssetType.TERMINAL,
      TERMINAL_ID_2,
      TERMINAL_NAME_2,
      AssetRole.PRIMARY,
      2,
      PRIMARY_OPERATOR_OU_ID_1,
      CACHED_PRIMARY_OPERATOR_NAME_1
  );

  public static AssetView assetView2 = new AssetView(
      1,
      fieldAsset2.getAssetNo(),
      fieldAsset2.getCachedAssetName(),
      fieldAsset2.getCachedAssetOperatorName(),
      fieldAsset2Licence1.getCachedLicenceRef() + ", " + fieldAsset2Licence2.getCachedLicenceRef(),
      BASE_ASSETS_URL + "/" + fieldAsset2.getAssetNo() + "/delete"
  );

  public static AssetView assetView3 = new AssetView(
      2,
      fieldAsset3.getAssetNo(),
      fieldAsset3.getCachedAssetName(),
      fieldAsset3.getCachedAssetOperatorName(),
      fieldAsset3Licence1.getCachedLicenceRef() + ", " + fieldAsset3Licence2.getCachedLicenceRef(),
      BASE_ASSETS_URL + "/" + fieldAsset3.getAssetNo() + "/delete"
  );

  public static List<AssetView> assetViews = List.of(assetView2, assetView3);

  public static List<ApplicationAsset> secondaryAssets = List.of(fieldAsset2, fieldAsset3);

  public static Map<ApplicationAsset, List<ApplicationAssetLicence>> secondaryAssetsLicencesMap = Map.of(
      fieldAsset2, fieldAsset2Licences,
      fieldAsset3, fieldAsset3Licences
  );

  public static List<ApplicationAssetLicence> allAssetsLicences = List.of(
      fieldAsset1Licence1, fieldAsset1Licence2,
      fieldAsset2Licence1, fieldAsset2Licence2,
      fieldAsset3Licence1, fieldAsset3Licence2
  );

}
