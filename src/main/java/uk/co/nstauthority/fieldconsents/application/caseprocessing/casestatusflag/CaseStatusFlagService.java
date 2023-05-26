package uk.co.nstauthority.fieldconsents.application.caseprocessing.casestatusflag;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
public class CaseStatusFlagService {

  public Set<CaseStatusFlag> getCaseStatusFlags(ApplicationVersion applicationVersion) {
    var caseStatusFlags = new HashSet<CaseStatusFlag>();

    if (Objects.nonNull(applicationVersion.getCaseOfficerWuaId())) {
      caseStatusFlags.add(CaseStatusFlag.CASE_OFFICER_ASSIGNED);
    } else {
      caseStatusFlags.add(CaseStatusFlag.CASE_OFFICER_NOT_ASSIGNED);
    }

    return caseStatusFlags;
  }
}
