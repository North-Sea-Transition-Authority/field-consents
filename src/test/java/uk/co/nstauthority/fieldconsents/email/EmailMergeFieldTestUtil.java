package uk.co.nstauthority.fieldconsents.email;

import java.util.Set;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.AssignmentTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.teams.Team;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.teams.TeamView;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole;

public class EmailMergeFieldTestUtil {

  public static final String APPLICATION_VERSION_DOMAIN_REFERENCE = "APPLICATION_VERSION";
  public static final String PRIMARY_OPERATOR_NAME_MAIL_MERGE_FIELD = "PRIMARY_OPERATOR_NAME";

  public static final ServiceUserDetail CASE_OFFICER = ServiceUserDetailTestUtil.Builder()
      .withForename("Case")
      .withSurname("Officer")
      .withEmailAddress("case.officer@email.co.uk")
      .build();

  public static final EnergyPortalUserDto CASE_OFFICER_EPU = AssignmentTestUtil.ENERGY_PORTAL_USER_1;

  public static final ServiceUserDetail CASE_MANAGER_1 = ServiceUserDetailTestUtil.Builder()
      .withForename("Case1")
      .withSurname("Manager1")
      .withEmailAddress("case.manager1@email.co.uk")
      .withWuaId(2L)
      .build();

  public static final ServiceUserDetail CASE_MANAGER_2 = ServiceUserDetailTestUtil.Builder()
      .withForename("Case2")
      .withSurname("Manager2")
      .withEmailAddress("case.manager2@email.co.uk")
      .withWuaId(3L)
      .build();

  public static final ServiceUserDetail CAM_USER = ServiceUserDetailTestUtil.Builder()
      .withForename("CamName")
      .withSurname("CamSurname")
      .withEmailAddress("cam.user@email.co.uk")
      .withWuaId(4L)
      .build();

  public static final Team REGULATOR_TEAM = new TeamTestUtil.TeamBuilder()
      .withId(1)
      .withTeamType(TeamType.REGULATOR)
      .build();

  public static final TeamMemberView TEAM_MEMBER_VIEW_CASE_MANAGER_1 = new TeamMemberView(
      WebUserAccountId.from(CASE_MANAGER_1),
      new TeamView(REGULATOR_TEAM.toTeamId(), TeamType.REGULATOR, "Regulator team"),
      "Mr",
      "Case1",
      "Manager1",
      "case.manager1@email.co.uk",
      "012345",
      Set.of(RegulatorTeamRole.CASE_MANAGER)
  );

  public static final TeamMemberView TEAM_MEMBER_VIEW_CASE_MANAGER_2 = new TeamMemberView(
      WebUserAccountId.from(CASE_MANAGER_1),
      new TeamView(REGULATOR_TEAM.toTeamId(), TeamType.REGULATOR, "Regulator team"),
      "Mr",
      "Case2",
      "Manager2",
      "case.manager2@email.co.uk",
      "06789",
      Set.of(RegulatorTeamRole.CASE_MANAGER)
  );
}
