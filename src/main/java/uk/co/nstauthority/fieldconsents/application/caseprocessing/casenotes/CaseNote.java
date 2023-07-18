package uk.co.nstauthority.fieldconsents.application.caseprocessing.casenotes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "application_case_notes")
public class CaseNote {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Long addedByWuaId;

  private Instant addedDateTime;

  private String caseNoteText;

  public CaseNote() {
  }

  public CaseNote(Integer id) {
    this.id = id;
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

  public Long getAddedByWuaId() {
    return addedByWuaId;
  }

  public void setAddedByWuaId(Long addedByWuaId) {
    this.addedByWuaId = addedByWuaId;
  }

  public Instant getAddedDateTime() {
    return addedDateTime;
  }

  public void setAddedDateTime(Instant addedDateTime) {
    this.addedDateTime = addedDateTime;
  }

  public String getCaseNoteText() {
    return caseNoteText;
  }

  public void setCaseNoteText(String caseNoteText) {
    this.caseNoteText = caseNoteText;
  }
}
