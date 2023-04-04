package uk.co.nstauthority.fieldconsents.teams;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "teams")
public class Team {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "type")
  @Enumerated(EnumType.STRING)
  private TeamType teamType;

  private String displayName;

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

}
