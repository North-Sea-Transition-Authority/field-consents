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
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.TERMINAL_NAME_1;

import java.util.List;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assetlicences.ApplicationAssetLicence;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.assets.AssetType;
import uk.co.nstauthority.fieldconsents.licences.LicenceTestUtil;

public class ApplicationAssetTestUtil {

  public static String BASE_ASSETS_URL = "/applications/" + ApplicationTestUtil.APPLICATION_ID + "/additional-assets";
  public static final ApplicationAsset fieldAsset1 = newBuilder()
      .withId(1)
      .withApplicationVersion(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT))
      .withAssetType(AssetType.FIELD)
      .withAssetId(FIELD_ID_1)
      .withCachedAssetName(FIELD_NAME_1)
      .withAssetRole(AssetRole.PRIMARY)
      .withAssetNo(null)
      .withAssetOperatorOuId(PRIMARY_OPERATOR_OU_ID_1)
      .withCachedAssetOperatorName(CACHED_PRIMARY_OPERATOR_NAME_1)
      .build();

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

  public static final ApplicationAsset fieldAsset2 = newBuilder()
      .withId(2)
      .withApplicationVersion(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT))
      .withAssetType(AssetType.FIELD)
      .withAssetId(FIELD_ID_2)
      .withCachedAssetName(FIELD_NAME_2)
      .withAssetRole(AssetRole.SECONDARY)
      .withAssetNo(2)
      .withAssetOperatorOuId(PRIMARY_OPERATOR_OU_ID_2)
      .withCachedAssetOperatorName(CACHED_PRIMARY_OPERATOR_NAME_2)
      .build();

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

  public static final ApplicationAsset fieldAsset3 = newBuilder()
      .withId(3)
      .withApplicationVersion(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT))
      .withAssetType(AssetType.FIELD)
      .withAssetId(FIELD_ID_3)
      .withCachedAssetName(FIELD_NAME_3)
      .withAssetRole(AssetRole.SECONDARY)
      .withAssetNo(3)
      .withAssetOperatorOuId(PRIMARY_OPERATOR_OU_ID_3)
      .withCachedAssetOperatorName(CACHED_PRIMARY_OPERATOR_NAME_3)
      .build();

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

  public static final ApplicationAsset terminalAsset1 = newBuilder()
      .withId(1)
      .withApplicationVersion(ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.VENT))
      .withAssetType(AssetType.TERMINAL)
      .withAssetId(TERMINAL_ID_1)
      .withCachedAssetName(TERMINAL_NAME_1)
      .withAssetRole(AssetRole.PRIMARY)
      .withAssetNo(1)
      .withAssetOperatorOuId(PRIMARY_OPERATOR_OU_ID_1)
      .withCachedAssetOperatorName(CACHED_PRIMARY_OPERATOR_NAME_1)
      .build();

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

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private Integer id = 1;
    private ApplicationVersion applicationVersion = ApplicationTestUtil.getNewApplicationVersionWithType(ApplicationType.PRODUCTION);
    private AssetType assetType = AssetType.FIELD;
    private Integer assetId = 2;
    private String cachedAssetName = "Asset name";
    private AssetRole assetRole = AssetRole.HOST;
    private Integer assetNo = 3;
    private Integer assetOperatorOuId = 4;
    private String cachedAssetOperatorName = "operator name";

    public Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    public Builder withApplicationVersion(ApplicationVersion applicationVersion) {
      this.applicationVersion = applicationVersion;
      return this;
    }

    public Builder withAssetType(AssetType assetType) {
      this.assetType = assetType;
      return this;
    }

    public Builder withAssetId(Integer assetId) {
      this.assetId = assetId;
      return this;
    }

    public Builder withCachedAssetName(String cachedAssetName) {
      this.cachedAssetName = cachedAssetName;
      return this;
    }

    public Builder withAssetRole(AssetRole assetRole) {
      this.assetRole = assetRole;
      return this;
    }

    public Builder withAssetNo(Integer assetNo) {
      this.assetNo = assetNo;
      return this;
    }

    public Builder withAssetJson(AssetJson assetJson) {
      this.assetId = assetJson.getId();
      this.assetType = assetJson.getAssetType();

      return this;
    }

    public Builder withApplicationAsset(ApplicationAsset applicationAsset) {
      this.assetNo = applicationAsset.getAssetNo();
      this.assetRole = applicationAsset.getAssetRole();

      return this;
    }

    public Builder withAssetOperatorOuId(Integer assetOperatorOuId) {
      this.assetOperatorOuId = assetOperatorOuId;
      return this;
    }

    public Builder withCachedAssetOperatorName(String cachedAssetOperatorName) {
      this.cachedAssetOperatorName = cachedAssetOperatorName;
      return this;
    }

    public ApplicationAsset build() {
      var applicationAsset = new ApplicationAsset();

      applicationAsset.setId(id);
      applicationAsset.setApplicationVersion(applicationVersion);
      applicationAsset.setAssetType(assetType);
      applicationAsset.setAssetId(assetId);
      applicationAsset.setCachedAssetName(cachedAssetName);
      applicationAsset.setAssetRole(assetRole);
      applicationAsset.setAssetNo(assetNo);
      applicationAsset.setAssetOperatorOuId(assetOperatorOuId);
      applicationAsset.setCachedAssetOperatorName(cachedAssetOperatorName);

      return applicationAsset;
    }
  }

}
