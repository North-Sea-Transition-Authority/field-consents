package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import io.micrometer.observation.annotation.Observed;
import java.util.Collection;
import java.util.List;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewQueryService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemViewService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class BulkCaseActionService {

  private final ApplicationDataItemViewService applicationDataItemService;
  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final ApplicationDataItemViewQueryService applicationDataItemQueryService;

  BulkCaseActionService(
      ApplicationDataItemViewService applicationDataItemService,
      ApplicationDataItemDtoService applicationDataItemDtoService,
      ApplicationDataItemViewQueryService applicationDataItemQueryService
  ) {
    this.applicationDataItemService = applicationDataItemService;
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.applicationDataItemQueryService = applicationDataItemQueryService;
  }

  public List<ApplicationDataItemView> getSelectedApplicationDataItemViews(
      Collection<Integer> selectedApplicationIds,
      ServiceUserDetail user
  ) {
    return getApplicationDataItemViews(user, List.of(APPLICATIONS.ID.in(selectedApplicationIds)));
  }

  @Observed(name = "fcs.database.bulk-case-actions-query", contextualName = "bulk case actions query executed")
  public List<ApplicationDataItemView> getApplicationDataItemViews(ServiceUserDetail user, List<Condition> conditions) {
    var dtos = applicationDataItemQueryService.runQueryWithCustom(
        conditions,
        selectQuery -> selectQuery.addOrderBy(
            greatest(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME, APPLICATION_VERSIONS.CREATED_DATE_TIME).desc())
    );

    var organisationUnitJsons = applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(dtos);

    return applicationDataItemService.getItemViewsFromDtos(dtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

}
