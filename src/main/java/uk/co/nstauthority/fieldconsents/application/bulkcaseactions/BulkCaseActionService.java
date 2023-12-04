package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.Collections;
import java.util.List;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.bulkcaseactions.assigncaseofficer.BulkAssignCaseOfficerController;
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

  public List<ApplicationDataItem> getSelectedApplicationDataItems(
      BulkCaseActionSelectedApplicationsForm form,
      ServiceUserDetail user
  ) {
    var selectedApplicationIds = form.selectedApplicationIds();
    return getApplicationDataItems(user, List.of(APPLICATIONS.ID.in(selectedApplicationIds)));
  }

  public List<ApplicationDataItem> getApplicationDataItems(ServiceUserDetail user) {
    return getApplicationDataItems(user, Collections.emptyList());
  }

  public List<ApplicationDataItem> getApplicationDataItems(ServiceUserDetail user, List<Condition> conditions) {
    var dtos = applicationDataItemQueryService.runQueryWithCustom(
        conditions,
        selectQuery -> selectQuery.addOrderBy(
            greatest(APPLICATION_VERSIONS.SUBMITTED_DATE_TIME, APPLICATION_VERSIONS.CREATED_DATE_TIME).desc())
    );

    var organisationUnitJsons = applicationDataItemDtoService.getOrganisationUnitJsonsFromApplicationDataItemDtos(dtos);

    return applicationDataItemService.getItemsFromDtos(dtos, organisationUnitJsons, TeamType.REGULATOR, user);
  }

  List<String> getBulkActions() {
    return List.of(
        BulkAssignCaseOfficerController.ASSIGN_CASE_OFFICER
    );
  }

}
