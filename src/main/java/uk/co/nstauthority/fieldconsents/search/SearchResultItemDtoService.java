package uk.co.nstauthority.fieldconsents.search;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.List;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemQueryService;

@Service
public class SearchResultItemDtoService {

  private final ApplicationDataItemQueryService applicationDataItemQueryService;

  SearchResultItemDtoService(ApplicationDataItemQueryService applicationDataItemQueryService) {
    this.applicationDataItemQueryService = applicationDataItemQueryService;
  }

  List<ApplicationDataItemDto> runSearchQuery(List<Condition> conditions) {
    return applicationDataItemQueryService.runQueryWithCustom(conditions, selectQuery ->
      selectQuery.addOrderBy(greatest(
          APPLICATION_VERSIONS.SUBMITTED_DATE_TIME,
          APPLICATION_VERSIONS.CREATED_DATE_TIME).desc()),
      ApplicationDataItemDto.class);
  }
}
