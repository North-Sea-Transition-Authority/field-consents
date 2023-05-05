package uk.co.nstauthority.fieldconsents.workarea;

import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.jooq.Condition;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;

@Service
public class WorkAreaFilterService {

  ArrayList<Condition> getConditions(WorkAreaFilter filter)  {
    var conditions = new ArrayList<Condition>();

    if (Objects.nonNull(filter.getStatuses())) {
      conditions.add(getStatusQueryCondition(filter.getStatuses()));
    }

    if (StringUtils.isNotBlank(filter.getReference())) {
      conditions.add(getReferenceQueryCondition(filter.getReference()));
    }

    if (Objects.nonNull(filter.getApplicationTypes())) {
      conditions.add(getApplicationTypesQueryCondition(filter.getApplicationTypes()));
    }

    return conditions;
  }

  private Condition getStatusQueryCondition(List<ApplicationVersionStatus> statuses) {
    var statusStrings = statuses
        .stream()
        .map(ApplicationVersionStatus::getEnumName)
        .toList();
    return APPLICATION_VERSIONS.STATUS.in(statusStrings);
  }

  // TODO: Implement this when case reference ticket FCS-323 is resolved
  private Condition getReferenceQueryCondition(String reference) {
    return null;
  }

  private Condition getApplicationTypesQueryCondition(List<ApplicationType> applicationTypes) {
    var applicationTypeStrings = applicationTypes
        .stream()
        .map(ApplicationType::getEnumName)
        .toList();
    return APPLICATIONS.TYPE.in(applicationTypeStrings);
  }
}
