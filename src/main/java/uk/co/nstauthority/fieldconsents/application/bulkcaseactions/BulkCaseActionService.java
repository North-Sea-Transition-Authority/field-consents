package uk.co.nstauthority.fieldconsents.application.bulkcaseactions;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.application.bulkcaseactions.BulkCaseActionSearchController.FORM_SESSION_ATTRIBUTE;
import static uk.co.nstauthority.fieldconsents.generated.jooq.Tables.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import jakarta.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.jooq.Condition;
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

  public List<Integer> getSelectedApplicationIds(HttpSession httpSession) {
    return Optional.ofNullable(httpSession.getAttribute(FORM_SESSION_ATTRIBUTE))
        .filter(BulkCaseActionSearchForm.class::isInstance)
        .map(BulkCaseActionSearchForm.class::cast)
        .map(BulkCaseActionSearchForm::selectedApplicationIds)
        .stream()
        .flatMap(Collection::stream)
        .map(Integer::parseInt)
        .distinct()
        .toList();
  }

  public List<ApplicationDataItem> getSelectedApplicationDataItems(HttpSession httpSession, ServiceUserDetail user) {
    var selectedApplicationIds = getSelectedApplicationIds(httpSession);
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

}
