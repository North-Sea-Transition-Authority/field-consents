package uk.co.nstauthority.fieldconsents.application.caseprocessing;

import java.util.List;
import java.util.Map;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDtoTestUtil;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamMemberViewTestUtil;
import uk.co.nstauthority.fieldconsents.teams.management.view.TeamMemberView;

public class AssignmentTestUtil {

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_1 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(1L)
          .withForename("Forename1")
          .withSurname("Surname1")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_2 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(2L)
          .withForename("Forename2")
          .withSurname("Surname2")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_3 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(3L)
          .withForename("Forename3")
          .withSurname("Surname3")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_4 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(4L)
          .withForename("Forename4")
          .withSurname("Surname4")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_5 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(5L)
          .withForename("Forename5")
          .withSurname("Surname5")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_6 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(6L)
          .withForename("Forename6")
          .withSurname("Surname6")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_7 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(7L)
          .withForename("Forename7")
          .withSurname("Surname7")
          .build();

  public static final EnergyPortalUserDto ENERGY_PORTAL_USER_8 =
      EnergyPortalUserDtoTestUtil.Builder()
          .withWebUserAccountId(8L)
          .withForename("Forename8")
          .withSurname("Surname8")
          .build();

  public static final ServiceUserDetail SERVICE_USER_DETAIL_USER_1 =
      ServiceUserDetail.from(ENERGY_PORTAL_USER_1);

  public static final TeamMemberView CASE_OFFICER_TEAM_MEMBER_VIEW_1 =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.CASE_OFFICER)
          .withWuaId(ENERGY_PORTAL_USER_1.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_1.forename())
          .withSurname(ENERGY_PORTAL_USER_1.surname())
          .build();

  public static final TeamMemberView VIEWER_TEAM_MEMBER_VIEW =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.VIEWER)
          .withWuaId(ENERGY_PORTAL_USER_2.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_2.forename())
          .withSurname(ENERGY_PORTAL_USER_2.surname())
          .build();

  public static final TeamMemberView CASE_OFFICER_TEAM_MEMBER_VIEW_2 =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.CASE_OFFICER)
          .withWuaId(ENERGY_PORTAL_USER_3.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_3.forename())
          .withSurname(ENERGY_PORTAL_USER_3.surname())
          .build();

  public static final TeamMemberView ACCESS_MANGER_TEAM_MEMBER_VIEW =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.ACCESS_MANAGER)
          .withWuaId(ENERGY_PORTAL_USER_4.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_4.forename())
          .withSurname(ENERGY_PORTAL_USER_4.surname())
          .build();

  public static final ServiceUserDetail SERVICE_USER_DETAIL_USER_5 =
      ServiceUserDetail.from(ENERGY_PORTAL_USER_5);

  public static final TeamMemberView TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1 =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.TECHNICAL_REVIEWER)
          .withWuaId(ENERGY_PORTAL_USER_5.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_5.forename())
          .withSurname(ENERGY_PORTAL_USER_5.surname())
          .build();

  public static final ServiceUserDetail SERVICE_USER_DETAIL_USER_6 =
      ServiceUserDetail.from(ENERGY_PORTAL_USER_6);

  public static final TeamMemberView TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2 =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.TECHNICAL_REVIEWER)
          .withWuaId(ENERGY_PORTAL_USER_6.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_6.forename())
          .withSurname(ENERGY_PORTAL_USER_6.surname())
          .build();

  public static final TeamMemberView CAM_USER_TEAM_MEMBER_VIEW_1 =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
          .withWuaId(ENERGY_PORTAL_USER_7.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_7.forename())
          .withSurname(ENERGY_PORTAL_USER_7.surname())
          .build();

  public static final TeamMemberView CAM_USER_TEAM_MEMBER_VIEW_2 =
      TeamMemberViewTestUtil.newBuilder()
          .withRoles(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER)
          .withWuaId(ENERGY_PORTAL_USER_8.webUserAccountId())
          .withForename(ENERGY_PORTAL_USER_8.forename())
          .withSurname(ENERGY_PORTAL_USER_8.surname())
          .build();

  public static final List<TeamMemberView> TEAM_MEMBER_VIEW_LIST =
      List.of(
          CASE_OFFICER_TEAM_MEMBER_VIEW_1,
          VIEWER_TEAM_MEMBER_VIEW,
          CASE_OFFICER_TEAM_MEMBER_VIEW_2,
          ACCESS_MANGER_TEAM_MEMBER_VIEW,
          TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1,
          TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2,
          CAM_USER_TEAM_MEMBER_VIEW_1,
          CAM_USER_TEAM_MEMBER_VIEW_2
      );

  public static final List<TeamMemberView> CASE_OFFICER_ASSIGNMENT_CANDIDATES =
      List.of(CASE_OFFICER_TEAM_MEMBER_VIEW_1, CASE_OFFICER_TEAM_MEMBER_VIEW_2);

  public static final Map<String, String> CASE_OFFICER_ASSIGNMENT_CANDIDATES_MAP =
      Map.of(
          CASE_OFFICER_TEAM_MEMBER_VIEW_1.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_1.getDisplayName(),
          CASE_OFFICER_TEAM_MEMBER_VIEW_2.wuaId().toString(), CASE_OFFICER_TEAM_MEMBER_VIEW_2.getDisplayName()
      );

  public static final List<TeamMemberView> TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES =
      List.of(TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1, TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2);

  public static final Map<String, String> TECHNICAL_REVIEWER_ASSIGNMENT_CANDIDATES_MAP =
      Map.of(
          TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1.wuaId().toString(),
          TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_1.getDisplayName(),
          TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2.wuaId().toString(),
          TECHNICAL_REVIEWER_TEAM_MEMBER_VIEW_2.getDisplayName()
      );

  public static final List<TeamMemberView> CAM_USER_ASSIGNMENT_CANDIDATES =
      List.of(CAM_USER_TEAM_MEMBER_VIEW_1, CAM_USER_TEAM_MEMBER_VIEW_2);

  public static final Map<String, String> CAM_USER_ASSIGNMENT_CANDIDATES_MAP =
      Map.of(
          CAM_USER_TEAM_MEMBER_VIEW_1.wuaId().toString(), CAM_USER_TEAM_MEMBER_VIEW_1.getDisplayName(),
          CAM_USER_TEAM_MEMBER_VIEW_2.wuaId().toString(), CAM_USER_TEAM_MEMBER_VIEW_2.getDisplayName()
      );
}
