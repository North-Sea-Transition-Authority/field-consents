package uk.co.nstauthority.fieldconsents.licences;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Licence;

public class LicenceTestUtil {

  public static final Integer LICENCE_ID_1 = 1;
  public static final Integer LICENCE_ID_2 = 2;
  public static final Integer LICENCE_ID_3 = 3;

  public static final String LICENCE_REF_1 = "P1";
  public static final String LICENCE_REF_2 = "P2";
  public static final String LICENCE_REF_3 = "P3";

  public static Licence licence1 = Licence.newBuilder().id(LICENCE_ID_1).licenceRef(LICENCE_REF_1).build();

  public static LicenceJson licence1Json = new LicenceJson(licence1.getId(), licence1.getLicenceRef());

  public static Licence licence2 = Licence.newBuilder().id(LICENCE_ID_2).licenceRef(LICENCE_REF_2).build();

  public static LicenceJson licence2Json = new LicenceJson(licence2.getId(), licence2.getLicenceRef());

  public static Licence licence3 = Licence.newBuilder().id(LICENCE_ID_3).licenceRef(LICENCE_REF_3).build();

  public static LicenceJson licence3Json = new LicenceJson(licence3.getId(), licence3.getLicenceRef());

  public static List<Licence> licences1 = List.of(licence1);

  public static List<Licence> licences2 = List.of(licence1, licence2);

  public static List<Licence> licences3 = List.of(licence1, licence2, licence3);

}
