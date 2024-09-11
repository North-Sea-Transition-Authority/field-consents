package uk.co.nstauthority.fieldconsents.application.eiadirection;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Audited
@Table(name = "application_eia_directions")
public class EiaDirection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Boolean haveSubmittedEiaDirection;

  private Integer satId;

  private String cachedSatRef;

  private Boolean forPurposeOfEiaRegs;

  private String rationaleForPurposeOfEiaRegs;

  private Boolean haveEiaDirectionToSubmit;

  private LocalDate latestDateToBeSubmitted;

  private String whyNoEiaDirection;

  public EiaDirection() {
  }

  public EiaDirection(
      Integer id,
      ApplicationVersion applicationVersion,
      Boolean haveSubmittedEiaDirection,
      Integer satId,
      String cachedSatRef,
      Boolean forPurposeOfEiaRegs,
      String rationaleForPurposeOfEiaRegs,
      Boolean haveEiaDirectionToSubmit,
      LocalDate latestDateToBeSubmitted,
      String whyNoEiaDirection
  ) {
    this.id = id;
    this.applicationVersion = applicationVersion;
    this.haveSubmittedEiaDirection = haveSubmittedEiaDirection;
    this.satId = satId;
    this.cachedSatRef = cachedSatRef;
    this.forPurposeOfEiaRegs = forPurposeOfEiaRegs;
    this.rationaleForPurposeOfEiaRegs = rationaleForPurposeOfEiaRegs;
    this.haveEiaDirectionToSubmit = haveEiaDirectionToSubmit;
    this.latestDateToBeSubmitted = latestDateToBeSubmitted;
    this.whyNoEiaDirection = whyNoEiaDirection;
  }

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Boolean getHaveSubmittedEiaDirection() {
    return haveSubmittedEiaDirection;
  }

  public void setHaveSubmittedEiaDirection(Boolean haveSubmittedEiaDirection) {
    this.haveSubmittedEiaDirection = haveSubmittedEiaDirection;
  }

  public Integer getSatId() {
    return satId;
  }

  public void setSatId(Integer satId) {
    this.satId = satId;
  }

  public String getCachedSatRef() {
    return cachedSatRef;
  }

  public void setCachedSatRef(String cachedSatRef) {
    this.cachedSatRef = cachedSatRef;
  }

  public Boolean getForPurposeOfEiaRegs() {
    return forPurposeOfEiaRegs;
  }

  public void setForPurposeOfEiaRegs(Boolean forPurposeOfEiaRegs) {
    this.forPurposeOfEiaRegs = forPurposeOfEiaRegs;
  }

  public String getRationaleForPurposeOfEiaRegs() {
    return rationaleForPurposeOfEiaRegs;
  }

  public void setRationaleForPurposeOfEiaRegs(String rationaleForPurposeOfEiaRegs) {
    this.rationaleForPurposeOfEiaRegs = rationaleForPurposeOfEiaRegs;
  }

  public Boolean getHaveEiaDirectionToSubmit() {
    return haveEiaDirectionToSubmit;
  }

  public void setHaveEiaDirectionToSubmit(Boolean haveEiaDirectionToSubmit) {
    this.haveEiaDirectionToSubmit = haveEiaDirectionToSubmit;
  }

  public LocalDate getLatestDateToBeSubmitted() {
    return latestDateToBeSubmitted;
  }

  public void setLatestDateToBeSubmitted(LocalDate latestDateToBeSubmitted) {
    this.latestDateToBeSubmitted = latestDateToBeSubmitted;
  }

  public String getWhyNoEiaDirection() {
    return whyNoEiaDirection;
  }

  public void setWhyNoEiaDirection(String whyNoEiaDirection) {
    this.whyNoEiaDirection = whyNoEiaDirection;
  }
}
