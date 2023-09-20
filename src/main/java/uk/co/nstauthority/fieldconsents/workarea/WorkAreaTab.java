package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ALLOCATE_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.ASSIGN_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.PROCESS_FCS_APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.RESPOND_TO_CONSULTATION;
import static uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission.TECHNICAL_REVIEW_FCS_APPLICATIONS;

import java.util.EnumSet;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

public enum WorkAreaTab {
  MY_APPLICATIONS(
      "My applications",
      "myApplications",
      "my-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCaseOfficerMyApplications(null, null)),
      10,
      EnumSet.of(PROCESS_FCS_APPLICATIONS),
      ApplicationWorkAreaPriorityGroup.REGULATOR),
  MY_TECHNICAL_REVIEWS(
      "My technical reviews",
      "myTechnicalReviews",
      "my-technical-reviews",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaMyTechnicalReviews(null, null)),
      20,
      EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS),
      ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER),
  ALL_TECHNICAL_REVIEWS(
      "All technical reviews",
      "allTechnicalReviews",
      "all-technical-reviews",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaAllTechnicalReviews(null, null)),
      30,
      EnumSet.of(TECHNICAL_REVIEW_FCS_APPLICATIONS),
      ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER),
  ALL_APPLICATIONS(
      "All applications",
      "allApplications",
      "all-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaRegulatorAllApplications(null, null)),
      40,
      EnumSet.of(ASSIGN_FCS_APPLICATIONS),
      ApplicationWorkAreaPriorityGroup.REGULATOR),
  UNASSIGNED_APPLICATIONS(
      "Unassigned",
      "unassigned",
      "unassigned",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCaseOfficerUnassignedApplications(null, null)),
      50,
      EnumSet.of(PROCESS_FCS_APPLICATIONS, ASSIGN_FCS_APPLICATIONS),
      ApplicationWorkAreaPriorityGroup.REGULATOR),
  ALL_CONSULTATIONS(
      "All consultations",
      "allConsultations",
      "all-consultations",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaAllConsultations(null, null)),
      60,
      EnumSet.of(ALLOCATE_CONSULTATION),
      ApplicationWorkAreaPriorityGroup.CONSULTEE),
  UNASSIGNED_CONSULTATIONS(
      "Unassigned consultations",
      "unassignedConsultations",
      "unassigned-consultations",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaUnassignedConsultations(null, null)),
      70,
      EnumSet.of(ALLOCATE_CONSULTATION),
      ApplicationWorkAreaPriorityGroup.CONSULTEE),
  MY_CONSULTATIONS(
      "My consultations",
      "myConsultations",
      "my-consultations",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaMyConsultations(null, null)),
      80,
      EnumSet.of(RESPOND_TO_CONSULTATION),
      ApplicationWorkAreaPriorityGroup.CONSULTEE)
  ;

  private final String label;
  private final String value;
  private final String anchor;
  private final String url;
  private final int displayOrder;
  private final Set<RolePermission> rolePermissions;
  private final ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup;

  WorkAreaTab(String label, String value, String anchor, String url, int displayOrder, Set<RolePermission> rolePermissions,
              ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup) {
    this.label = label;
    this.value = value;
    this.anchor = anchor;
    this.displayOrder = displayOrder;
    this.url = url;
    this.rolePermissions = rolePermissions;
    this.applicationWorkAreaPriorityGroup = applicationWorkAreaPriorityGroup;
  }

  public String getLabel() {
    return label;
  }

  public String getValue() {
    return value;
  }

  public String getAnchor() {
    return anchor;
  }

  public String getUrl() {
    return url;
  }

  public int getDisplayOrder() {
    return displayOrder;
  }

  public Set<RolePermission> getRolePermissions() {
    return rolePermissions;
  }

  public ApplicationWorkAreaPriorityGroup getApplicationWorkAreaPriorityGroup() {
    return applicationWorkAreaPriorityGroup;
  }
}
