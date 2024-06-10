package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Audited
@Table(name = "application_supporting_information")
public class SupportingInformation {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private String notes;

  private String erapNotes;

  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public String getErapNotes() {
    return erapNotes;
  }

  public void setErapNotes(String erapNotes) {
    this.erapNotes = erapNotes;
  }

  public static SupportingInformation from(ApplicationVersion applicationVersion,
                                           SupportingInformationForm supportingInformationForm) {
    SupportingInformation supportingInformation = new SupportingInformation();
    supportingInformation.setApplicationVersion(applicationVersion);
    supportingInformation.setNotes(supportingInformationForm.getNotes().getInputValue());
    supportingInformation.setErapNotes(supportingInformationForm.getErapNotes().getInputValue());
    return supportingInformation;
  }
}
