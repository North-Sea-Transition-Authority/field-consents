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

import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
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
import org.springframework.mock.web.MockHttpSession;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemQueryService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class BulkCaseActionServiceTest {

  @Mock
  private ApplicationDataItemService applicationDataItemService;

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @Mock
  private ApplicationDataItemQueryService applicationDataItemQueryService;

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
  void getSelectedApplicationIds() {
    var session = new MockHttpSession();
    session.setAttribute(BulkCaseActionSearchController.FORM_SESSION_ATTRIBUTE, new BulkCaseActionSearchForm(List.of("1", "2", "3")));

    assertThat(bulkCaseActionService.getSelectedApplicationIds(session)).containsExactly(1, 2, 3);
  }

  @Test
  void getSelectedApplicationIds_FormNotInSession() {
    var session = new MockHttpSession();
    assertThat(bulkCaseActionService.getSelectedApplicationIds(session)).isEmpty();
  }

  @Test
  void getSelectedApplicationIds_AttributeOfWrongTypeInSession() {
    var session = new MockHttpSession();
    session.setAttribute(BulkCaseActionSearchController.FORM_SESSION_ATTRIBUTE, "1, 2, 3, I am not a form");

    assertThat(bulkCaseActionService.getSelectedApplicationIds(session)).isEmpty();
  }

  @Test
  void getSelectedApplicationIds_DoesNotContainDuplicates() {
    var session = new MockHttpSession();
    session.setAttribute(BulkCaseActionSearchController.FORM_SESSION_ATTRIBUTE, new BulkCaseActionSearchForm(List.of("1", "1")));

    assertThat(bulkCaseActionService.getSelectedApplicationIds(session)).containsExactly(1);
  }

  @Test
  void getSelectedApplicationDataItems() {
    var selectedIds = List.of(1, 2, 3);
    doReturn(selectedIds).when(bulkCaseActionService).getSelectedApplicationIds(any(HttpSession.class));

    var applicationDataItems = List.of(ApplicationDataItem.newBuilder().build());
    doReturn(applicationDataItems).when(bulkCaseActionService).getApplicationDataItems(any(ServiceUserDetail.class), anyList());

    var session = new MockHttpSession();
    assertThat(bulkCaseActionService.getSelectedApplicationDataItems(session, user)).containsExactlyElementsOf(applicationDataItems);

    verify(bulkCaseActionService).getSelectedApplicationIds(session);
    verify(bulkCaseActionService).getApplicationDataItems(user, List.of(APPLICATIONS.ID.in(selectedIds)));
  }

  @Test
  void getApplicationDataItems_withoutConditions() {
    var applicationDataItems = List.of(ApplicationDataItem.newBuilder().build());

    doReturn(applicationDataItems).when(bulkCaseActionService).getApplicationDataItems(any(ServiceUserDetail.class), anyList());

    assertThat(bulkCaseActionService.getApplicationDataItems(user)).containsExactlyElementsOf(applicationDataItems);

    verify(bulkCaseActionService).getApplicationDataItems(user, Collections.emptyList());
  }

  @Test
  void getApplicationDataItems_withConditions() {
    var dtos = List.of(mock(ApplicationDataItemDto.class));
    when(applicationDataItemQueryService.runQueryWithCustom(conditionsCaptor.capture(), selectQueryCaptor.capture())).thenReturn(dtos);

    var organisationUnitJson = List.of(mock(OrganisationUnitJson.class));
    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(dtos)).thenReturn(organisationUnitJson);

    var applicationDataItems = List.of(mock(ApplicationDataItem.class));
    when(applicationDataItemService.getItemsFromDtos(dtos, organisationUnitJson, TeamType.REGULATOR, user))
        .thenReturn(applicationDataItems);

    var conditions = List.of(APPLICATIONS.ID.in(1, 2, 3));
    assertThat(bulkCaseActionService.getApplicationDataItems(user, conditions)).isEqualTo(applicationDataItems);

    var selectQuery = mock(SelectQuery.class);
    selectQueryCaptor.getValue().accept(selectQuery);
    verify(selectQuery).addOrderBy(greatest(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME, APPLICATION_VERSIONS.CREATED_DATE_TIME).desc());

    assertThat(conditionsCaptor.getValue()).containsExactlyElementsOf(conditions);
  }
}
