package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Entity
@Table(name = "application_withdrawals")
public class ApplicationWithdrawal {

  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id
  private Integer id;

  @OneToOne
  @JoinColumn(name = "application_version_id")
  private ApplicationVersion applicationVersion;

  private Long requestedByWuaId;

  private Instant requestedDateTime;

  private String requestText;

  private Long respondedByWuaId;

  private Instant respondedDateTime;

  private String responseText;

  @Enumerated(EnumType.STRING)
  private WithdrawalStatus withdrawalStatus;


  public Integer getId() {
    return id;
  }

  public ApplicationVersion getApplicationVersion() {
    return applicationVersion;
  }

  public void setApplicationVersion(ApplicationVersion applicationVersion) {
    this.applicationVersion = applicationVersion;
  }

  public Long getRequestedByWuaId() {
    return requestedByWuaId;
  }

  public void setRequestedByWuaId(Long requestedByWuaId) {
    this.requestedByWuaId = requestedByWuaId;
  }

  public Instant getRequestedDateTime() {
    return requestedDateTime;
  }

  public void setRequestedDateTime(Instant requestedDateTime) {
    this.requestedDateTime = requestedDateTime;
  }

  public String getRequestText() {
    return requestText;
  }

  public void setRequestText(String requestText) {
    this.requestText = requestText;
  }

  public Long getRespondedByWuaId() {
    return respondedByWuaId;
  }

  public void setRespondedByWuaId(Long respondedByWuaId) {
    this.respondedByWuaId = respondedByWuaId;
  }

  public Instant getRespondedDateTime() {
    return respondedDateTime;
  }

  public void setRespondedDateTime(Instant respondedDateTime) {
    this.respondedDateTime = respondedDateTime;
  }

  public String getResponseText() {
    return responseText;
  }

  public void setResponseText(String responseText) {
    this.responseText = responseText;
  }

  public WithdrawalStatus getWithdrawalStatus() {
    return withdrawalStatus;
  }

  public void setWithdrawalStatus(WithdrawalStatus withdrawalStatus) {
    this.withdrawalStatus = withdrawalStatus;
  }
}
