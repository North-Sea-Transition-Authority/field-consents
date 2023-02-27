package uk.co.nstauthority.fieldconsents.application.eiadirection;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
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

  private Boolean haveEiaDirectionToSubmit;

  private LocalDate latestDateToBeSubmitted;

  private String whyNoEiaDirection;

  public static EiaDirection from(ApplicationVersion applicationVersion,
                                  EiaDirectionForm eiaDirectionForm,
                                  String cachedSatRef) {
    var eiaDirection = new EiaDirection();
    eiaDirection.setApplicationVersion(applicationVersion);
    eiaDirection.setHaveSubmittedEiaDirection(eiaDirectionForm.getHaveSubmittedEiaDirection());

    // only set the appropriate data from the form (just in case extra data has been entered)

    if (Boolean.TRUE.equals(eiaDirectionForm.getHaveSubmittedEiaDirection())) {
      eiaDirection.setSatId(eiaDirectionForm.getSatId());
      eiaDirection.setCachedSatRef(cachedSatRef);
      return eiaDirection;
    }

    eiaDirection.setHaveEiaDirectionToSubmit(eiaDirectionForm.getHaveEiaDirectionToSubmit());

    if (Boolean.TRUE.equals(eiaDirectionForm.getHaveEiaDirectionToSubmit())) {
      eiaDirection.setLatestDateToBeSubmitted(
          eiaDirectionForm.getLatestDateToBeSubmitted().getAsLocalDate().orElseThrow(NoSuchElementException::new)
      );
      return eiaDirection;
    }

    eiaDirection.setWhyNoEiaDirection(eiaDirectionForm.getWhyNoEiaDirection().getInputValue());

    return eiaDirection;
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
