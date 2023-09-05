package uk.co.nstauthority.fieldconsents.search;

import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;

public class SearchResultItem extends ApplicationDataItem {

  private final String licences;

  public SearchResultItem(Integer applicationId, String type, String duration, String reference, String operator,
                          String asset, String geographicArea, String status, String submittedDateTime,
                          String submittedBy, String aceFlag, String caseOfficer, Boolean withdrawalOpen,
                          String technicalReviewer, Boolean applicationUpdateOpen, String applicationUpdateDeadline,
                          Boolean consultationOpen, String consultationDeadline,
                          String licences) {

    super(applicationId, type, duration, reference, operator, asset, geographicArea, status, submittedDateTime,
        submittedBy, aceFlag, caseOfficer, withdrawalOpen, technicalReviewer, applicationUpdateOpen,
        applicationUpdateDeadline, consultationOpen, consultationDeadline);

    this.licences = licences;
  }

  public String getLicences() {
    return licences;
  }
}
