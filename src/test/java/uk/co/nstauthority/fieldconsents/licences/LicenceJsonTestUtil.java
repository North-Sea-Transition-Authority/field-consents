package uk.co.nstauthority.fieldconsents.licences;

import java.time.LocalDate;
import java.util.List;
import uk.co.fivium.energyportalapi.generated.types.Licence;

public class LicenceJsonTestUtil {

  public static final Integer LICENCE_ID_1 = 1;
  public static final Integer LICENCE_ID_2 = 2;
  public static final Integer LICENCE_ID_3 = 3;

  public static final String LICENCE_TYPE_1 = "P";
  public static final String LICENCE_TYPE_2 = "P";
  public static final String LICENCE_TYPE_3 = "P";

  public static final Integer LICENCE_NO_1 = 1;
  public static final Integer LICENCE_NO_2 = 2;
  public static final Integer LICENCE_NO_3 = 3;

  public static final String LICENCE_REF_1 = "P1";
  public static final String LICENCE_REF_2 = "P2";
  public static final String LICENCE_REF_3 = "P3";

  public static final LocalDate SCHEDULE_EXPIRY_DATE_1 =
      LocalDate.of(2024, 8,1);
  public static final LocalDate SCHEDULE_EXPIRY_DATE_2 =
      LocalDate.of(2024, 8,2);
  public static final LocalDate SCHEDULE_EXPIRY_DATE_3 =
      LocalDate.of(2024, 8,3);

  public static Licence licence1 = Licence.newBuilder()
      .id(LICENCE_ID_1)
      .licenceType(LICENCE_TYPE_1)
      .licenceNo(LICENCE_NO_1)
      .licenceRef(LICENCE_REF_1)
      .scheduleExpiryDate(SCHEDULE_EXPIRY_DATE_1)
      .build();

  public static LicenceJson licence1Json = new LicenceJson(
      licence1.getId(),
      licence1.getLicenceType(),
      licence1.getLicenceNo(),
      licence1.getLicenceRef(),
      licence1.getScheduleExpiryDate()
  );

  public static Licence licence2 = Licence.newBuilder()
      .id(LICENCE_ID_2)
      .licenceType(LICENCE_TYPE_2)
      .licenceNo(LICENCE_NO_2)
      .licenceRef(LICENCE_REF_2)
      .scheduleExpiryDate(SCHEDULE_EXPIRY_DATE_2)
      .build();

  public static LicenceJson licence2Json = new LicenceJson(
      licence2.getId(),
      licence2.getLicenceType(),
      licence2.getLicenceNo(),
      licence2.getLicenceRef(),
      licence2.getScheduleExpiryDate()
  );

  public static Licence licence3 = Licence.newBuilder()
      .id(LICENCE_ID_3)
      .licenceType(LICENCE_TYPE_3)
      .licenceNo(LICENCE_NO_3)
      .licenceRef(LICENCE_REF_3)
      .scheduleExpiryDate(SCHEDULE_EXPIRY_DATE_3)
      .build();

  public static LicenceJson licence3Json = new LicenceJson(
      licence3.getId(),
      licence3.getLicenceType(),
      licence3.getLicenceNo(),
      licence3.getLicenceRef(),
      licence3.getScheduleExpiryDate()
  );

  public static List<Licence> licences1 = List.of(licence1);

  public static List<Licence> licences2 = List.of(licence1, licence2);

  public static List<Licence> licences3 = List.of(licence1, licence2, licence3);

  static Builder builder() {
    return new Builder();
  }

  static class Builder {

    private Integer id = 100;
    private String licenceType = "licence type";
    private Integer licenceNo = 20;
    private String licenceRef = "licence reference";
    private LocalDate scheduleExpiryDate = LocalDate.now().plusMonths(12);

    Builder withId(Integer id) {
      this.id = id;
      return this;
    }

    Builder withLicenceType(String licenceType) {
      this.licenceType = licenceType;
      return this;
    }

    Builder withLicenceNumber(Integer licenceNumber) {
      this.licenceNo = licenceNumber;
      return this;
    }

    Builder withLicenceReference(String licenceReference) {
      this.licenceRef = licenceReference;
      return this;
    }

    LicenceJson build() {
      return new LicenceJson(
          id,
          licenceType,
          licenceNo,
          licenceRef,
          scheduleExpiryDate
      );
    }
  }
}
