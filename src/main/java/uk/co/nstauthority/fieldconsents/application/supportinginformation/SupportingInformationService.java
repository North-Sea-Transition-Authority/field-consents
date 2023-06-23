package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class SupportingInformationService {

  private final SupportingInformationRepository supportingInformationRepository;
  private final SupportingInformationDocumentService supportingInformationDocumentService;

  @Autowired
  public SupportingInformationService(SupportingInformationRepository supportingInformationRepository,
                                      SupportingInformationDocumentService supportingInformationDocumentService) {
    this.supportingInformationRepository = supportingInformationRepository;
    this.supportingInformationDocumentService = supportingInformationDocumentService;
  }

  public SupportingInformationForm getSupportingInformationForm(ApplicationVersion applicationVersion) {
    return supportingInformationRepository.findByApplicationVersion(applicationVersion)
        .map(supportingInformation ->
            SupportingInformationForm.from(
                supportingInformation,
                supportingInformationDocumentService.getUploadedFiles(applicationVersion)
            )
        )
        .orElse(new SupportingInformationForm());
  }

  public Optional<SupportingInformation> findSupportingInformation(ApplicationVersion applicationVersion) {
    return supportingInformationRepository.findByApplicationVersion(applicationVersion);
  }

  @Transactional
  public void saveSupportingInformation(ApplicationVersion applicationVersion, SupportingInformationForm form) {
    supportingInformationRepository.deleteByApplicationVersion(applicationVersion);
    supportingInformationDocumentService.saveDocuments(applicationVersion, form.getSupportingDocuments());
    supportingInformationRepository.save(SupportingInformation.from(applicationVersion, form));
  }

  public SummaryCard getSupportingInformationSummaryCard(ApplicationVersion applicationVersion) {
    var supportingInformationOptional = findSupportingInformation(applicationVersion);

    if (supportingInformationOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var supportingInformation = supportingInformationOptional.get();

    var summaryData = SummaryDataView.newWithKeyValue("Notes", supportingInformation.getNotes());

    if (ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION.allowed(applicationVersion.getApplication().getType())) {
      summaryData.addKeyValue("ERAP alignment studies and projects", supportingInformation.getErapNotes());
    }

    return SummaryCard.simpleSummaryCard(summaryData);
  }

}
