package uk.co.nstauthority.fieldconsents.teams;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import uk.co.fivium.digitalnotificationlibrary.core.notification.DomainReference;

@Entity
@Table(name = "teams")
public class Team implements DomainReference {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TeamType teamType;

  private String displayName;

  private Integer organisationGroupId;

  public Team() {
  }

  public Team(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public TeamType getTeamType() {
    return teamType;
  }

  public void setTeamType(TeamType teamType) {
    this.teamType = teamType;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  public Integer getOrganisationGroupId() {
    return organisationGroupId;
  }

  public void setOrganisationGroupId(Integer organisationGroupId) {
    this.organisationGroupId = organisationGroupId;
  }

  public TeamId toTeamId() {
    return new TeamId(this.getId());
  }

  @Override
  public String toString() {
    return "Team{" +
        "id=" + id +
        ", teamType=" + teamType +
        ", displayName='" + displayName + '\'' +
        '}';
  }

  @Override
  public String getDomainId() {
    return String.valueOf(id);
  }

  @Override
  public String getDomainType() {
    return "TEAM";
  }
}
