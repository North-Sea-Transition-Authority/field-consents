package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;

@ExtendWith(MockitoExtension.class)
class BulkCaseActionControllerHelperServiceTest {

  @InjectMocks
  private BulkCaseActionControllerHelperService controllerHelperService;

  private HttpSession session;

  @BeforeEach
  void setUp() {
    session = new MockHttpSession();
  }

  @Test
  void getSelectedApplicationsForm() {
    var form = BulkCaseActionSelectedApplicationsForm.empty();
    session.setAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE, form);

    assertThat(controllerHelperService.getSelectedApplicationsForm(session)).isEqualTo(form);
  }

  @Test
  void getSelectedApplicationsForm_doesNotExistInSession() {
    assertThat(controllerHelperService.getSelectedApplicationsForm(session))
        .isEqualTo(BulkCaseActionSelectedApplicationsForm.empty());
  }

  @Test
  void getSelectedApplicationsForm_withApplicationDataItemViews() {
    var selectedApplicationIds = Set.of("1", "2", "3");
    var form = new BulkCaseActionSelectedApplicationsForm(selectedApplicationIds);
    session.setAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE, form);

    var applicationDataItemViews = List.of(
        applicationDataItemBuilderWithDefaults(1).build(),
        applicationDataItemBuilderWithDefaults(2).build(),
        applicationDataItemBuilderWithDefaults(3).build(),
        applicationDataItemBuilderWithDefaults(4).build(),
        applicationDataItemBuilderWithDefaults(5).build()
    );

    assertThat(controllerHelperService.getSelectedApplicationsForm(session, applicationDataItemViews))
        .isEqualTo(new BulkCaseActionSelectedApplicationsForm(selectedApplicationIds));
  }

  @Test
  void getSelectedApplicationsForm_withNoSelectedApplications() {
    var selectedApplicationIds = Collections.<String>emptySet();
    var form = new BulkCaseActionSelectedApplicationsForm(selectedApplicationIds);
    session.setAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE, form);

    var applicationDataItemViews = List.of(
        applicationDataItemBuilderWithDefaults(1).build(),
        applicationDataItemBuilderWithDefaults(2).build(),
        applicationDataItemBuilderWithDefaults(3).build(),
        applicationDataItemBuilderWithDefaults(4).build(),
        applicationDataItemBuilderWithDefaults(5).build()
    );

    assertThat(controllerHelperService.getSelectedApplicationsForm(session, applicationDataItemViews))
        .isEqualTo(BulkCaseActionSelectedApplicationsForm.empty());
  }

  @Test
  void getSelectedApplicationsForm_withApplicationDataItemViews_ensureAdditionalItemsNotIncluded() {
    var selectedApplicationIds = Set.of("1", "2", "3");
    var form = new BulkCaseActionSelectedApplicationsForm(selectedApplicationIds);
    session.setAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE, form);

    // only one is available, so it should 'unselect' 2 and 3
    var applicationDataItemViews = Collections.singletonList(applicationDataItemBuilderWithDefaults(1).build());

    assertThat(controllerHelperService.getSelectedApplicationsForm(session, applicationDataItemViews))
        .isEqualTo(new BulkCaseActionSelectedApplicationsForm(Collections.singleton("1")));
  }

  @Test
  void getSearchFiltersForm() {
    var searchFiltersForm = BulkCaseActionSearchFiltersForm.empty();
    session.setAttribute(BulkCaseActionControllerHelperService.FILTERS_FORM_SESSION_ATTRIBUTE, searchFiltersForm);
    assertThat(controllerHelperService.getSearchFiltersForm(session)).isEqualTo(searchFiltersForm);
  }

  @Test
  void getSearchFiltersForm_doesNotExistInSession() {
    assertThat(controllerHelperService.getSearchFiltersForm(session)).isEqualTo(BulkCaseActionSearchFiltersForm.empty());
  }

  @Test
  void updateSearchFilters() {
    var form = BulkCaseActionSearchFiltersForm.empty();
    controllerHelperService.updateSearchFilters(session, form);
    assertThat(session.getAttribute(BulkCaseActionControllerHelperService.FILTERS_FORM_SESSION_ATTRIBUTE)).isEqualTo(form);
  }

  @Test
  void clearSearchFilters() {
    var form = BulkCaseActionSearchFiltersForm.empty();
    session.setAttribute(BulkCaseActionControllerHelperService.FILTERS_FORM_SESSION_ATTRIBUTE, form);

    controllerHelperService.clearSearchFilters(session);
    assertThat(session.getAttribute(BulkCaseActionControllerHelperService.FILTERS_FORM_SESSION_ATTRIBUTE)).isNull();
  }

  @Test
  void clearSelectedApplicationsForm() {
    var form = BulkCaseActionSelectedApplicationsForm.empty();
    session.setAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE, form);

    controllerHelperService.clearSelectedApplicationsForm(session);
    assertThat(session.getAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE)).isNull();
  }

  @Test
  void updateSelectedApplicationsForm() {
    var form = BulkCaseActionSelectedApplicationsForm.empty();
    controllerHelperService.updateSelectedApplicationsForm(session, form);
    assertThat(session.getAttribute(BulkCaseActionControllerHelperService.SELECTED_APPLICATIONS_FORM_SESSION_ATTRIBUTE)).isEqualTo(form);
  }

  private ApplicationDataItemView.Builder applicationDataItemBuilderWithDefaults(Integer applicationId) {
    return ApplicationDataItemView.newBuilder()
        .withApplicationId(applicationId)
        .withType("")
        .withReference("")
        .withOperator("")
        .withDuration("")
        .withAceFlag("")
        .withAsset("")
        .withGeographicArea("")
        .withStatus("")
        .withCaseOfficer("")
        .withTechnicalReviewer("")
        .withSubmittedDateTime("")
        .withSubmittedBy("")
        .withLicences("");
  }

}
