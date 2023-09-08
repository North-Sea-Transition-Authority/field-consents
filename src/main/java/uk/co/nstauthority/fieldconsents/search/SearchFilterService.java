package uk.co.nstauthority.fieldconsents.search;

import static org.jooq.impl.DSL.exists;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationFlags.APPLICATION_FLAGS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.flags.ApplicationFlagType;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterService;

@Service
public class SearchFilterService {

  private final DSLContext context;
  private final ApplicationDataFilterService applicationDataFilterService;

  public SearchFilterService(DSLContext context,
                             ApplicationDataFilterService applicationDataFilterService) {
    this.context = context;
    this.applicationDataFilterService = applicationDataFilterService;
  }

  List<Condition> getConditions(SearchFilterForm form) {
    var applicationDataFilterConditions = applicationDataFilterService.getConditions(form);

    List<Condition> searchFilterConditions = new ArrayList<>(applicationDataFilterConditions);
    Optional.ofNullable(form.getAceFlagStatuses())
        .map(this::getAceStatusCondition)
        .ifPresent(searchFilterConditions::add);

    return searchFilterConditions;
  }

  private Condition getAceStatusCondition(List<AceFlagStatus> aceFlagStatuses) {
    var aceFlagIsAceApplicationValues = aceFlagStatuses
        .stream()
        .map(AceFlagStatus::isAceApplication)
        .toList();
    return exists(context.select(APPLICATION_FLAGS.FLAG_VALUE)
        .from(APPLICATION_FLAGS)
        .where(APPLICATION_FLAGS.FLAG_TYPE.eq(ApplicationFlagType.IS_ACE_APPLICATION.name())
            .and(APPLICATION_FLAGS.APPLICATION_VERSION_ID.eq(APPLICATION_VERSIONS.ID))
            .and(APPLICATION_FLAGS.FLAG_VALUE.in(aceFlagIsAceApplicationValues)))
    );
  }
}
