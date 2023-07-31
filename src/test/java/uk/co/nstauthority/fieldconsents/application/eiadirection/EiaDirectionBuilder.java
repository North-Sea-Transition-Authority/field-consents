package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.time.LocalDate;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

public class EiaDirectionBuilder {

  private Integer id;
  private ApplicationVersion applicationVersion;
  private Boolean haveSubmittedEiaDirection;
  private Integer satId;
  private String cachedSatRef;
  private Boolean forPurposeOfEiaRegs;
  private Boolean haveEiaDirectionToSubmit;
  private LocalDate latestDateToBeSubmitted;
  private String whyNoEiaDirection;

  private EiaDirectionBuilder() {
  }

  public static EiaDirectionBuilder newBuilder() {
    return new EiaDirectionBuilder();
  }

  public static EiaDirectionBuilder from(EiaDirection eiaDirection) {
    return newBuilder()
        .withId(eiaDirection.getId())
        .withApplicationVersion(eiaDirection.getApplicationVersion())
        .withHaveSubmittedEiaDirection(eiaDirection.getHaveSubmittedEiaDirection())
        .withSatId(eiaDirection.getSatId())
        .withCachedSatRef(eiaDirection.getCachedSatRef())
        .withForPurposeOfEiaRegs(eiaDirection.getForPurposeOfEiaRegs())
        .withHaveEiaDirectionToSubmit(eiaDirection.getHaveEiaDirectionToSubmit())
        .withLatestDateToBeSubmitted(eiaDirection.getLatestDateToBeSubmitted())
        .withWhyNoEiaDirection(eiaDirection.getWhyNoEiaDirection());
  }

  public EiaDirectionBuilder withId(Integer id) {
    this.id = id;
    return this;
  }

  public EiaDirectionBuilder withApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
    return this;
  }

  public EiaDirectionBuilder withHaveSubmittedEiaDirection(Boolean haveSubmittedEiaDirection) {
    this.haveSubmittedEiaDirection = haveSubmittedEiaDirection;
    return this;
  }

  public EiaDirectionBuilder withSatId(Integer satId) {
    this.satId = satId;
    return this;
  }

  public EiaDirectionBuilder withCachedSatRef(String cachedSatRef) {
    this.cachedSatRef = cachedSatRef;
    return this;
  }

  public EiaDirectionBuilder withForPurposeOfEiaRegs(Boolean forPurposeOfEiaRegs) {
    this.forPurposeOfEiaRegs = forPurposeOfEiaRegs;
    return this;
  }

  public EiaDirectionBuilder withHaveEiaDirectionToSubmit(Boolean haveEiaDirectionToSubmit) {
    this.haveEiaDirectionToSubmit = haveEiaDirectionToSubmit;
    return this;
  }

  public EiaDirectionBuilder withLatestDateToBeSubmitted(LocalDate latestDateToBeSubmitted) {
    this.latestDateToBeSubmitted = latestDateToBeSubmitted;
    return this;
  }

  public EiaDirectionBuilder withWhyNoEiaDirection(String whyNoEiaDirection) {
    this.whyNoEiaDirection = whyNoEiaDirection;
    return this;
  }

  public EiaDirection build() {
    return new EiaDirection(
        id,
        applicationVersion,
        haveSubmittedEiaDirection,
        satId,
        cachedSatRef,
        forPurposeOfEiaRegs,
        haveEiaDirectionToSubmit,
        latestDateToBeSubmitted,
        whyNoEiaDirection
    );
  }

}
