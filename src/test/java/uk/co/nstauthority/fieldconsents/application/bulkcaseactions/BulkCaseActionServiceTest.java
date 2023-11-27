package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jooq.impl.DSL.greatest;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

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
import org.mockito.junit.jupiter.MockitoExtension;
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
  void getApplicationDataItems() {
    var dtos = List.of(mock(ApplicationDataItemDto.class));
    when(applicationDataItemQueryService.runQueryWithCustom(conditionsCaptor.capture(), selectQueryCaptor.capture())).thenReturn(dtos);

    var organisationUnitJson = List.of(mock(OrganisationUnitJson.class));
    when(applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(dtos)).thenReturn(organisationUnitJson);

    var applicationDataItems = List.of(mock(ApplicationDataItem.class));
    when(applicationDataItemService.getItemsFromDtos(dtos, organisationUnitJson, TeamType.REGULATOR, user))
        .thenReturn(applicationDataItems);

    assertThat(bulkCaseActionService.getApplicationDataItems(user)).isEqualTo(applicationDataItems);

    var selectQuery = mock(SelectQuery.class);
    selectQueryCaptor.getValue().accept(selectQuery);
    verify(selectQuery)
        .addOrderBy(greatest(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME, APPLICATION_VERSIONS.CREATED_DATE_TIME).desc());

    assertThat(conditionsCaptor.getValue()).isEmpty();
  }
}
