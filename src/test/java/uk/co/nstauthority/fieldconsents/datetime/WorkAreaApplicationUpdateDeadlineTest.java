package uk.co.nstauthority.fieldconsents.datetime;

import static java.time.format.DateTimeFormatter.ofPattern;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.annotation.DirtiesContext.ClassMode.AFTER_CLASS;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperatorAndLicences;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import uk.co.nstauthority.fieldconsents.application.ApplicationRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationService;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionRepository;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.action.RoleGroup;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestController;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestForm;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestFormValidator;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.authentication.UserDetailService;
import uk.co.nstauthority.fieldconsents.integrationtest.AbstractIntegrationTest;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.teams.Role;
import uk.co.nstauthority.fieldconsents.teams.TeamQueryService;
import uk.co.nstauthority.fieldconsents.teams.TeamRoleTestUtil;
import uk.co.nstauthority.fieldconsents.teams.TeamType;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaController;
import uk.co.nstauthority.fieldconsents.workarea.WorkAreaFilter;

@Transactional
@DirtiesContext(classMode = AFTER_CLASS)
class WorkAreaApplicationUpdateDeadlineTest extends AbstractIntegrationTest {

  private static final ServiceUserDetail SERVICE_USER_DETAIL = ServiceUserDetailTestUtil.Builder().build();

  private int applicationNumber = 8000;

  @MockitoBean
  private UserDetailService userDetailService;

  @MockitoBean
  private ApplicationUpdateRequestFormValidator applicationUpdateRequestFormValidator;

  @MockitoBean
  private OrganisationUnitService organisationUnitService;

  @MockitoBean
  private TeamQueryService teamQueryService;

  @Autowired
  private ApplicationUpdateRequestController applicationUpdateRequestController;

  @Autowired
  private WorkAreaController workAreaController;

  @Autowired
  private ApplicationService applicationService;

  @Autowired
  private ApplicationRepository applicationRepository;

  @Autowired
  private ApplicationVersionRepository applicationVersionRepository;

  @BeforeEach
  void setUp() {
    when(userDetailService.getUserDetail()).thenReturn(SERVICE_USER_DETAIL);

    var teamRole = TeamRoleTestUtil.newBuilder().build();
    when(teamQueryService.getTeamRoles(SERVICE_USER_DETAIL))
        .thenReturn(List.of(teamRole));

    var regulatorRoles = new HashSet<>(RoleGroup.REGULATOR_CASE_PROCESSING_ROLES);
    regulatorRoles.add(Role.VIEWER);
    when(teamQueryService.userHasAtLeastOneStaticRole(SERVICE_USER_DETAIL, TeamType.REGULATOR, regulatorRoles))
        .thenReturn(true);

    when(organisationUnitService.getOrganisationUnitsByIds(anyList(), anyString())).thenReturn(Collections.singletonList(orgUnit1Json));
  }

  @ParameterizedTest(name = "Deadline {0} should be displayed as {1}")
  @MethodSource("applicationUpdateDeadlines")
  void requestApplicationUpdateAndCheckWorkAreaTag(LocalDateTime applicationUpdateDeadline, String expectedDeadline) {
    // create a submitted application version
    var applicationVersion = getSubmittedAndAssignedApplicationVersion();

    // request an update
    requestApplicationUpdate(applicationVersion, applicationUpdateDeadline);

    // check the values on the work area
    var dtos = getApplicationDataItemViewFromWorkArea();

    assertThat(dtos)
        .hasSize(1)
        .first()
        .extracting(ApplicationDataItemView::applicationUpdateDeadline)
        .isEqualTo(expectedDeadline);
  }

  // https://www.gov.uk/when-do-the-clocks-change
  private static Stream<Arguments> applicationUpdateDeadlines() {
    return Stream.of(
        // all
        arguments(LocalDateTime.of(2023, 1, 1, 12, 0), "1 Jan 2023 12:00"),
        arguments(LocalDateTime.of(2023, 1, 31, 12, 0), "31 Jan 2023 12:00"),
        arguments(LocalDateTime.of(2023, 2, 1, 12, 0), "1 Feb 2023 12:00"),
        arguments(LocalDateTime.of(2023, 2, 28, 12, 0), "28 Feb 2023 12:00"),
        arguments(LocalDateTime.of(2023, 3, 1, 12, 0), "1 Mar 2023 12:00"),
        arguments(LocalDateTime.of(2023, 3, 31, 12, 0), "31 Mar 2023 12:00"),
        arguments(LocalDateTime.of(2023, 4, 1, 12, 0), "1 Apr 2023 12:00"),
        arguments(LocalDateTime.of(2023, 4, 30, 12, 0), "30 Apr 2023 12:00"),
        arguments(LocalDateTime.of(2023, 5, 1, 12, 0), "1 May 2023 12:00"),
        arguments(LocalDateTime.of(2023, 5, 31, 12, 0), "31 May 2023 12:00"),
        arguments(LocalDateTime.of(2023, 6, 1, 12, 0), "1 Jun 2023 12:00"),
        arguments(LocalDateTime.of(2023, 6, 30, 12, 0), "30 Jun 2023 12:00"),
        arguments(LocalDateTime.of(2023, 7, 1, 12, 0), "1 Jul 2023 12:00"),
        arguments(LocalDateTime.of(2023, 7, 31, 12, 0), "31 Jul 2023 12:00"),
        arguments(LocalDateTime.of(2023, 8, 1, 12, 0), "1 Aug 2023 12:00"),
        arguments(LocalDateTime.of(2023, 8, 31, 12, 0), "31 Aug 2023 12:00"),
        arguments(LocalDateTime.of(2023, 9, 1, 12, 0), "1 Sept 2023 12:00"),
        arguments(LocalDateTime.of(2023, 9, 30, 12, 0), "30 Sept 2023 12:00"),
        arguments(LocalDateTime.of(2023, 10, 1, 12, 0), "1 Oct 2023 12:00"),
        arguments(LocalDateTime.of(2023, 10, 31, 12, 0), "31 Oct 2023 12:00"),
        arguments(LocalDateTime.of(2023, 11, 1, 12, 0), "1 Nov 2023 12:00"),
        arguments(LocalDateTime.of(2023, 11, 30, 12, 0), "30 Nov 2023 12:00"),
        arguments(LocalDateTime.of(2023, 12, 1, 12, 0), "1 Dec 2023 12:00"),
        arguments(LocalDateTime.of(2023, 12, 31, 12, 0), "31 Dec 2023 12:00"),

        // before BST
        arguments(LocalDateTime.of(2023, 3, 25, 12, 34), "25 Mar 2023 12:34"),
        arguments(LocalDateTime.of(2023, 3, 26, 0, 59), "26 Mar 2023 00:59"),

        // during BST - 26th March 2023 01:00 (clocks go forward 1hr)
        arguments(LocalDateTime.of(2023, 3, 26, 0, 30), "26 Mar 2023 00:30"),
        arguments(LocalDateTime.of(2023, 3, 26, 1, 0), "26 Mar 2023 02:00"),
        arguments(LocalDateTime.of(2023, 3, 26, 1, 1), "26 Mar 2023 02:01"),
        arguments(LocalDateTime.of(2023, 3, 26, 2, 0), "26 Mar 2023 02:00"),
        arguments(LocalDateTime.of(2023, 3, 26, 2, 1), "26 Mar 2023 02:01"),
        arguments(LocalDateTime.of(2023, 3, 26, 3, 0), "26 Mar 2023 03:00"),
        arguments(LocalDateTime.of(2023, 10, 29, 1, 0), "29 Oct 2023 01:00"),
        arguments(LocalDateTime.of(2023, 10, 29, 1, 59), "29 Oct 2023 01:59"),

        // after BST - 29th October 2023 02:00 (clocks go back 1hr)
        arguments(LocalDateTime.of(2023, 10, 29, 2, 0), "29 Oct 2023 02:00"),
        arguments(LocalDateTime.of(2023, 10, 29, 2, 1), "29 Oct 2023 02:01"),
        arguments(LocalDateTime.of(2023, 10, 29, 3, 0), "29 Oct 2023 03:00"),
        arguments(LocalDateTime.of(2023, 10, 30, 13, 49), "30 Oct 2023 13:49")
    );
  }

  private ApplicationVersion getSubmittedAndAssignedApplicationVersion() {
    var applicationVersion = applicationService.createNewApplicationForField(
        ApplicationType.FLARE,
        field1JsonWithOperatorAndLicences,
        orgUnit1Json,
        SERVICE_USER_DETAIL
    );

    var application = applicationVersion.getApplication();
    application.setApplicationNo(applicationNumber++);
    applicationRepository.save(application);

    applicationVersion.setCaseOfficerWuaId(SERVICE_USER_DETAIL.wuaId());
    applicationVersion.setCurrentCaseOwner(Role.CASE_OFFICER);
    applicationVersion.setSubmittedByWuaId(SERVICE_USER_DETAIL.wuaId());
    applicationVersion.setStatus(ApplicationVersionStatus.SUBMITTED);

    return applicationVersionRepository.save(applicationVersion);
  }

  private void requestApplicationUpdate(ApplicationVersion applicationVersion, LocalDateTime deadline) {
    var form = new ApplicationUpdateRequestForm();
    form.setDeadlineDate(deadline.format(ofPattern("dd/MM/yyyy")));
    form.setDeadlineHours(deadline.format(ofPattern("HH")));
    form.setDeadlineMinutes(deadline.format(ofPattern("mm")));
    form.getRequestText().setInputValue("comment");

    applicationUpdateRequestController.sendApplicationUpdateRequest(
        applicationVersion.getApplication().getId(),
        form,
        new BeanPropertyBindingResult(form, "form"),
        SERVICE_USER_DETAIL,
        new RedirectAttributesModelMap()
    );
  }

  @SuppressWarnings("unchecked")
  private List<ApplicationDataItemView> getApplicationDataItemViewFromWorkArea() {
    var modelAndView = workAreaController.getWorkArea(new WorkAreaFilter(), SERVICE_USER_DETAIL);
    assertThat(modelAndView.getModel()).containsKey("workAreaItems");

    return (List<ApplicationDataItemView>) modelAndView.getModel().get("workAreaItems");
  }
}
