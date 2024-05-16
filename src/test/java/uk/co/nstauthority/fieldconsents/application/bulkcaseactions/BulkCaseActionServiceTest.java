package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.greatest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import org.jooq.Condition;
import org.jooq.Record;
import org.jooq.SelectQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewQueryService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class BulkCaseActionServiceTest {

  @Mock
  private ApplicationDataItemViewService applicationDataItemService;

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private ApplicationDataItemViewQueryService applicationDataItemQueryService;

  @Spy
  @InjectMocks
  private BulkCaseActionService bulkCaseActionService;

  @Captor
  private ArgumentCaptor<Consumer<SelectQuery<Record>>> selectQueryCaptor;

  @Captor
  private ArgumentCaptor<List<Condition>> conditionsCaptor;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getSelectedApplicationDataItemViews() {
    var selectedIds = Set.of(1, 2, 3);
    var form = new BulkCaseActionSelectedApplicationsForm(Set.of("1", "2", "3"));

    var applicationDataItemViews = List.of(ApplicationDataItemView.newBuilder().build());
    doReturn(applicationDataItemViews).when(bulkCaseActionService).getApplicationDataItemViews(any(ServiceUserDetail.class), anyList());

    assertThat(bulkCaseActionService.getSelectedApplicationDataItemViews(form, user)).containsExactlyElementsOf(applicationDataItemViews);

    verify(bulkCaseActionService).getApplicationDataItemViews(user, List.of(APPLICATIONS.ID.in(selectedIds)));
  }

  @Test
  void getApplicationDataItemViews_withoutConditions() {
    var applicationDataItemViews = List.of(ApplicationDataItemView.newBuilder().build());

    doReturn(applicationDataItemViews).when(bulkCaseActionService).getApplicationDataItemViews(any(ServiceUserDetail.class), anyList());

    assertThat(bulkCaseActionService.getApplicationDataItemViews(user)).containsExactlyElementsOf(applicationDataItemViews);

    verify(bulkCaseActionService).getApplicationDataItemViews(user, Collections.emptyList());
  }

  @Test
  void getApplicationDataItemViews_withConditions() {
    var dtos = List.of(mock(ApplicationDataItemDto.class));
    when(applicationDataItemQueryService.runQueryWithCustom(conditionsCaptor.capture(), selectQueryCaptor.capture())).thenReturn(dtos);

    var organisationUnitJson = List.of(mock(OrganisationUnitJson.class));
    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(dtos)).thenReturn(organisationUnitJson);

    var applicationDataItemViews = List.of(mock(ApplicationDataItemView.class));
    when(applicationDataItemService.getItemViewsFromDtos(dtos, organisationUnitJson, TeamType.REGULATOR, user))
        .thenReturn(applicationDataItemViews);

    var conditions = List.of(APPLICATIONS.ID.in(1, 2, 3));
    assertThat(bulkCaseActionService.getApplicationDataItemViews(user, conditions)).isEqualTo(applicationDataItemViews);

    var selectQuery = mock(SelectQuery.class);
    selectQueryCaptor.getValue().accept(selectQuery);
    verify(selectQuery).addOrderBy(greatest(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME, APPLICATION_VERSIONS.CREATED_DATE_TIME).desc());

    assertThat(conditionsCaptor.getValue()).containsExactlyElementsOf(conditions);
  }

  @Test
  void getBulkActions() {
    assertThat(bulkCaseActionService.getBulkActions()).containsExactly(
        BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER
    );
  }
}
