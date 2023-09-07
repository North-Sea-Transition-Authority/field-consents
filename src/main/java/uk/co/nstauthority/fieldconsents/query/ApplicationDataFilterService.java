package uk.co.nstauthority.fieldconsents.query;

import static org.apache.commons.lang3.StringUtils.isNumeric;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ApplicationVersions.APPLICATION_VERSIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.Applications.APPLICATIONS;
import static uk.co.nstauthority.fieldconsents.generated.jooq.tables.ConsentLengths.CONSENT_LENGTHS;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionStatus;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthType;

@Service
public class ApplicationDataFilterService {

  public Condition getApplicationNumberQueryCondition(Integer applicationNumber) {
    return APPLICATIONS.APPLICATION_NO.eq(applicationNumber);
  }

  public Condition getStatusQueryCondition(List<ApplicationVersionStatus> statuses) {
    var statusStrings = statuses
        .stream()
        .map(ApplicationVersionStatus::getEnumName)
        .toList();
    return APPLICATION_VERSIONS.STATUS.in(statusStrings);
  }

  public Condition getApplicationTypesQueryCondition(List<ApplicationType> applicationTypes) {
    var applicationTypeStrings = applicationTypes
        .stream()
        .map(ApplicationType::getEnumName)
        .toList();
    return APPLICATIONS.TYPE.in(applicationTypeStrings);
  }

  public Condition getDurationTypesQueryCondition(List<ConsentLengthType> durationTypes) {
    var consentLengthStrings = durationTypes
        .stream()
        .map(ConsentLengthType::getEnumName)
        .toList();
    return CONSENT_LENGTHS.CONSENT_LENGTH.in(consentLengthStrings);
  }

  public Condition getOperatorCondition(Integer operatorId) {
    return APPLICATION_VERSIONS.PRIMARY_OPERATOR_OU_ID.eq(operatorId);
  }

  public List<Condition> getConditions(ApplicationDataFilterForm dataFilterForm) {
    var conditions = new ArrayList<Condition>();

    Optional.ofNullable(dataFilterForm.getStatuses())
        .map(this::getStatusQueryCondition)
        .ifPresent(conditions::add);

    var referenceNumber = dataFilterForm.getReferenceNumber();
    if (Objects.nonNull(referenceNumber)) {
      if (isNumeric(referenceNumber)) {
        conditions.add(this.getApplicationNumberQueryCondition(Integer.parseInt(referenceNumber)));
      } else {
        conditions.add(DSL.falseCondition());
      }
    }

    Optional.ofNullable(dataFilterForm.getApplicationTypes())
        .map(this::getApplicationTypesQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(dataFilterForm.getDurationTypes())
        .map(this::getDurationTypesQueryCondition)
        .ifPresent(conditions::add);

    Optional.ofNullable(dataFilterForm.getOperatorId())
        .map(this::getOperatorCondition)
        .ifPresent(conditions::add);

    return conditions;
  }
}
