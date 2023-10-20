package uk.co.nstauthority.fieldconsents.workarea;

import org.springframework.stereotype.Service;

@Service
public class WorkAreaFilterFormService {

  public WorkAreaFilterForm getFromFilter(WorkAreaFilter filter) {
    var form = new WorkAreaFilterForm();
    form.setReferenceNumber(filter.getReferenceNumber());
    form.setStatuses(filter.getStatuses());
    form.setApplicationTypes(filter.getApplicationTypes());
    form.setDurationTypes(filter.getDurationTypes());
    form.setAssetKey(filter.getAssetKey());
    form.setOperatorId(filter.getOperatorId());
    form.setGeographicAreas(filter.getGeographicAreas());
    form.setAssetTypesWithShore(filter.getAssetTypesWithShore());
    form.setSubmittedYear(filter.getSubmittedYear());
    form.setCaseOfficerWuaId(filter.getCaseOfficerWuaId());
    form.setTechnicalReviewerWuaId(filter.getTechnicalReviewerWuaId());
    return form;
  }
}
