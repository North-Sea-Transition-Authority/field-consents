package uk.co.nstauthority.fieldconsents.petsapplications;

import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.PetsApplication;
import uk.co.fivium.energyportalapi.generated.types.SatDecision;
import uk.co.fivium.energyportalapi.generated.types.SatStatus;
import uk.co.fivium.energyportalapi.generated.types.SatType;

public class PetsApplicationTestUtil {

  public static final Integer SAT_ID_1 = 1;
  public static final Integer SAT_ID_2 = 2;
  public static final Integer SAT_ID_3 = 3;
  public static final String SAT_REF_1 = "EIA/111/1";
  public static final String SAT_REF_2 = "EIA/222/2";
  public static final String SAT_REF_3 = "EIA/333/3";

  public static PetsApplication petsApplication1 =
      PetsApplication.newBuilder()
          .satId(SAT_ID_1)
          .satRef(SAT_REF_1)
          .satType(SatType.EIA_DIRECTION)
          .status(SatStatus.COMPLETED)
          .decision(SatDecision.APPROVE)
          .build();

  public static PetsApplicationJson petsApplication1Json =
      new PetsApplicationJson(
          petsApplication1.getSatId(),
          petsApplication1.getSatRef(),
          petsApplication1.getSatType(),
          petsApplication1.getStatus(),
          petsApplication1.getDecision()
      );

  public static PetsApplication petsApplication2 =
      PetsApplication.newBuilder()
          .satId(SAT_ID_2)
          .satRef(SAT_REF_2)
          .satType(SatType.EIA_DIRECTION_2020)
          .status(SatStatus.DECISION)
          .decision(SatDecision.UNKNOWN)
          .build();

  public static PetsApplicationJson petsApplication2Json =
      new PetsApplicationJson(
          petsApplication2.getSatId(),
          petsApplication2.getSatRef(),
          petsApplication2.getSatType(),
          petsApplication2.getStatus(),
          petsApplication2.getDecision()
      );

  public static PetsApplication petsApplication3 =
      PetsApplication.newBuilder()
          .satId(SAT_ID_3)
          .satRef(SAT_REF_3)
          .satType(SatType.EIA_DIRECTION)
          .status(SatStatus.COMPLETED)
          .decision(SatDecision.REJECT)
          .build();

  public static PetsApplicationJson petsApplication3Json =
      new PetsApplicationJson(
          petsApplication3.getSatId(),
          petsApplication3.getSatRef(),
          petsApplication3.getSatType(),
          petsApplication3.getStatus(),
          petsApplication3.getDecision()
      );

  public static List<PetsApplication> petsApplications =
      List.of(petsApplication1, petsApplication2, petsApplication3);

}
