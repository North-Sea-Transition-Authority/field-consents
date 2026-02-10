package uk.co.nstauthority.fieldconsents.workarea;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.EnumSet;
import java.util.Set;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

public enum WorkAreaTab implements Displayable {

  MY_APPLICATIONS(
      "My applications",
      "myApplications",
      "my-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCaseOfficerMyApplications(null, null)),
      10,
      EnumSet.of(Role.CASE_OFFICER),
      ApplicationWorkAreaPriorityGroup.REGULATOR
  ),
  MY_TECHNICAL_REVIEWS(
      "My technical reviews",
      "myTechnicalReviews",
      "my-technical-reviews",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaMyTechnicalReviews(null, null)),
      20,
      EnumSet.of(Role.TECHNICAL_REVIEWER),
      ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER
  ),
  ALL_TECHNICAL_REVIEWS(
      "All technical reviews",
      "allTechnicalReviews",
      "all-technical-reviews",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaAllTechnicalReviews(null, null)),
      30,
      EnumSet.of(Role.TECHNICAL_REVIEWER),
      ApplicationWorkAreaPriorityGroup.REGULATOR_TECHNICAL_REVIEWER
  ),
  MY_CAM_APPLICATIONS(
      "My applications (CAM)",
      "camMyApplications",
      "cam-my-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCamMyApplications(null, null)),
      40,
      EnumSet.of(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER),
      ApplicationWorkAreaPriorityGroup.REGULATOR
  ),
  ALL_APPLICATIONS(
      "All applications",
      "allApplications",
      "all-applications",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaRegulatorAllApplications(null, null)),
      50,
      EnumSet.of(Role.CASE_MANAGER, Role.CONSENTS_AND_AUTHORISATIONS_MANAGER),
      ApplicationWorkAreaPriorityGroup.REGULATOR
  ),
  UNASSIGNED_APPLICATIONS(
      "Unassigned",
      "unassigned",
      "unassigned",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaCaseOfficerUnassignedApplications(null, null)),
      60,
      EnumSet.of(Role.CASE_OFFICER, Role.CASE_MANAGER),
      ApplicationWorkAreaPriorityGroup.REGULATOR
  ),
  UNASSIGNED_CONSULTATIONS(
      "Unassigned consultations",
      "unassignedConsultations",
      "unassigned-consultations",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaUnassignedConsultations(null, null)),
      70,
      EnumSet.of(Role.ALLOCATOR),
      ApplicationWorkAreaPriorityGroup.CONSULTEE
  ),
  ALL_CONSULTATIONS(
      "All consultations",
      "allConsultations",
      "all-consultations",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaAllConsultations(null, null)),
      80,
      EnumSet.of(Role.ALLOCATOR),
      ApplicationWorkAreaPriorityGroup.CONSULTEE
  ),
  MY_CONSULTATIONS(
      "My consultations",
      "myConsultations",
      "my-consultations",
      ReverseRouter.route(on(WorkAreaController.class).getWorkAreaMyConsultations(null, null)),
      90,
      EnumSet.of(Role.RESPONDER),
      ApplicationWorkAreaPriorityGroup.CONSULTEE
  )
  ;

  private final String displayName;
  private final String value;
  private final String anchor;
  private final String url;
  private final int displayOrder;
  private final Set<Role> roles;
  private final ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup;

  WorkAreaTab(
      String displayName,
      String value,
      String anchor,
      String url,
      int displayOrder,
      Set<Role> roles,
      ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup
  ) {
    this.displayName = displayName;
    this.value = value;
    this.anchor = anchor;
    this.displayOrder = displayOrder;
    this.url = url;
    this.roles = roles;
    this.applicationWorkAreaPriorityGroup = applicationWorkAreaPriorityGroup;
  }

  @Override
  public String getDisplayName() {
    return displayName;
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

  @Override
  public int getDisplayOrder() {
    return displayOrder;
  }

  public Set<Role> getRoles() {
    return roles;
  }

  public ApplicationWorkAreaPriorityGroup getApplicationWorkAreaPriorityGroup() {
    return applicationWorkAreaPriorityGroup;
  }
}
