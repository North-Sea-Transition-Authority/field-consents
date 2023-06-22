package uk.co.nstauthority.fieldconsents.application.caseprocessing.assignment;

import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.ACCESS_MANAGER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.CASE_OFFICER;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.regulator.RegulatorTeamRole.VIEWER;

import java.util.List;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberView;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;

public class CaseAssignmentTestUtil {

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_1 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(1L)
          .withForename("Forename1")
          .withSurname("Surname1")
          .build();

  static final EnergyPortalUserDto ENERGY_PORTAL_USER_2 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(2L)
          .withForename("Forename2")
          .withSurname("Surname2")
          .build();

  static final EnergyPortalUserDto ENERGY_PORTAL_USER_3 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(3L)
          .withForename("Forename3")
          .withSurname("Surname3")
          .build();

  static final EnergyPortalUserDto ENERGY_PORTAL_USER_4 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(4L)
          .withForename("Forename4")
          .withSurname("Surname4")
          .build();

  static final ServiceUserDetail SERVICE_USER_DETAIL_USER_1 =
      ServiceUserDetail.from(ENERGY_PORTAL_USER_1);

  static final TeamMemberView CASE_OFFICER_TEAM_MEMBER_VIEW_1 =
      TeamMemberViewTestUtil.Builder()
          .withRole(CASE_OFFICER)
          .withWebUserAccountId(new WebUserAccountId(ENERGY_PORTAL_USER_1.webUserAccountId()))
          .withFirstName(ENERGY_PORTAL_USER_1.forename())
          .withLastName(ENERGY_PORTAL_USER_1.surname())
          .build();

  static final TeamMemberView VIEWER_TEAM_MEMBER_VIEW =
      TeamMemberViewTestUtil.Builder()
          .withRole(VIEWER)
          .withWebUserAccountId(new WebUserAccountId(ENERGY_PORTAL_USER_2.webUserAccountId()))
          .withFirstName(ENERGY_PORTAL_USER_2.forename())
          .withLastName(ENERGY_PORTAL_USER_2.surname())
          .build();

  static final TeamMemberView CASE_OFFICER_TEAM_MEMBER_VIEW_2 =
      TeamMemberViewTestUtil.Builder()
          .withRole(CASE_OFFICER)
          .withWebUserAccountId(new WebUserAccountId(ENERGY_PORTAL_USER_3.webUserAccountId()))
          .withFirstName(ENERGY_PORTAL_USER_3.forename())
          .withLastName(ENERGY_PORTAL_USER_3.surname())
          .build();

  static final TeamMemberView ACCESS_MANGER_TEAM_MEMBER_VIEW =
      TeamMemberViewTestUtil.Builder()
          .withRole(ACCESS_MANAGER)
          .withWebUserAccountId(new WebUserAccountId(ENERGY_PORTAL_USER_4.webUserAccountId()))
          .withFirstName(ENERGY_PORTAL_USER_4.forename())
          .withLastName(ENERGY_PORTAL_USER_4.surname())
          .build();

  static final List<TeamMemberView> TEAM_MEMBER_VIEW_LIST =
      List.of(CASE_OFFICER_TEAM_MEMBER_VIEW_1, VIEWER_TEAM_MEMBER_VIEW, CASE_OFFICER_TEAM_MEMBER_VIEW_2,
          ACCESS_MANGER_TEAM_MEMBER_VIEW);

  static final List<TeamMemberView> CASE_OFFICER_ASSIGNMENT_CANDIDATES =
      List.of(CASE_OFFICER_TEAM_MEMBER_VIEW_1, CASE_OFFICER_TEAM_MEMBER_VIEW_2);

  static final Map<String, String> CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP =
      Map.of(
          CASE_OFFICER_TEAM_MEMBER_VIEW_1.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_1.getDisplayName(),
          CASE_OFFICER_TEAM_MEMBER_VIEW_2.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_2.getDisplayName()
      );
}
