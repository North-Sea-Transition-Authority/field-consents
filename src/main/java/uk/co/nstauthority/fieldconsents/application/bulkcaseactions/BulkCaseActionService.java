package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.Collections;
import java.util.List;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDtoService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemQueryService;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemService;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Service
public class BulkCaseActionService {

  private final ApplicationDataItemService applicationDataItemService;
  private final ApplicationDataItemDtoService applicationDataItemDtoService;
  private final ApplicationDataItemQueryService applicationDataItemQueryService;

  BulkCaseActionService(
      ApplicationDataItemService applicationDataItemService,
      ApplicationDataItemDtoService applicationDataItemDtoService,
      ApplicationDataItemQueryService applicationDataItemQueryService
  ) {
    this.applicationDataItemService = applicationDataItemService;
    this.applicationDataItemDtoService = applicationDataItemDtoService;
    this.applicationDataItemQueryService = applicationDataItemQueryService;
  }

  public List<ApplicationDataItem> getApplicationDataItems(ServiceUserDetail user) {
    var dtos = applicationDataItemQueryService.runQueryWithCustom(
        Collections.emptyList(),
        selectQuery -> selectQuery.addOrderBy(greatest(
            APPLICATION_VERSIONS.SUBMITTED_DATE_TIME,
            APPLICATION_VERSIONS.CREATED_DATE_TIME).desc())
    );

    var organisationUnitJsons = applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(dtos);

    return applicationDataItemService.getItemsFromDtos(dtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

}
