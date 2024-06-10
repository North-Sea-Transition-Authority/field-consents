package uk.co.nstauthority.fieldconsents.teams;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.envers.Audited;

@Entity
@Audited
@Table(name = "team_member_roles")
class TeamMemberRole {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @JoinColumn(name = "team_id")
  @ManyToOne
  private Team team;

  private Long wuaId;

  private String role;

  protected TeamMemberRole() {
  }

  TeamMemberRole(Integer id) {
    this.id = id;
  }

  public Integer getId() {
    return id;
  }

  public Team getTeam() {
    return team;
  }

  public void setTeam(Team team) {
    this.team = team;
  }

  public Long getWuaId() {
    return wuaId;
  }

  public void setWuaId(Long wuaId) {
    this.wuaId = wuaId;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  @Override
  public String toString() {
    return "TeamMemberRole{" +
        "id=" + id +
        ", team=" + team +
        ", wuaId=" + wuaId +
        ", role='" + role + '\'' +
        '}';
  }
}
