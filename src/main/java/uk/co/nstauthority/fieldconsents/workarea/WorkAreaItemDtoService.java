package uk.co.nstauthority.fieldconsents.workarea;

import static org.jooq.impl.DSL.greatest;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationWorkAreaPriorities.APPLICATION_WORK_AREA_PRIORITIES;

import java.util.List;
import org.jooq.Condition;
import org.jooq.JoinType;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemDto;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataItemQueryService;

@Service
public class WorkAreaItemDtoService {

  private final ApplicationDataItemQueryService applicationDataItemQueryService;

  WorkAreaItemDtoService(ApplicationDataItemQueryService applicationDataItemQueryService) {
    this.applicationDataItemQueryService = applicationDataItemQueryService;
  }

  List<ApplicationDataItemDto> runWorkAreaQuery(List<Condition> conditions,
                                                ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup) {

    return applicationDataItemQueryService.runQueryWithCustom(conditions, selectQuery ->  {
      selectQuery.addJoin(APPLICATION_WORK_AREA_PRIORITIES, JoinType.LEFT_OUTER_JOIN,
          APPLICATION_WORK_AREA_PRIORITIES.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID)
              .and(APPLICATION_WORK_AREA_PRIORITIES.WORK_AREA_PRIORITY_GROUP.eq(applicationWorkAreaPriorityGroup.name())));
      selectQuery.addOrderBy(greatest(
          // if the work area priority date is not set for the priority group then fallback to the other dates
          APPLICATION_WORK_AREA_PRIORITIES.WORK_AREA_PRIORITY_DATE_TIME,
          APPLICATION_VERSIONS.SUBMITTED_DATE_TIME,
          APPLICATION_VERSIONS.CREATED_DATE_TIME).desc());
    });
  }
}
