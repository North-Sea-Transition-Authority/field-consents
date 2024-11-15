package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionFileUsage;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@Service
public class SupportingInformationService {

  private final SupportingInformationRepository supportingInformationRepository;
  private final FieldConsentsFileService fieldConsentsFileService;

  @Autowired
  public SupportingInformationService(SupportingInformationRepository supportingInformationRepository,
                                      FieldConsentsFileService fieldConsentsFileService) {
    this.supportingInformationRepository = supportingInformationRepository;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  public SupportingInformationForm getSupportingInformationForm(ApplicationVersion applicationVersion) {
    return supportingInformationRepository.findByApplicationVersion(applicationVersion)
        .map(supportingInformation ->
            SupportingInformationForm.from(
                supportingInformation,
                fieldConsentsFileService.getUploadedFiles(getFileUsage(applicationVersion))
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
    fieldConsentsFileService.saveDocuments(getFileUsage(applicationVersion), form.getDocuments());
    supportingInformationRepository.save(SupportingInformation.from(applicationVersion, form));
  }

  public List<SummaryCard> getSupportingInformationSummaryCards(ApplicationVersion applicationVersion) {
    var supportingInformationOptional = findSupportingInformation(applicationVersion);

    if (supportingInformationOptional.isEmpty()) {
      return SummaryCard.emptySummaryCardList();
    }

    var supportingInformation = supportingInformationOptional.get();

    var summaryData = SummaryDataView.newWithKeyValue("Notes", supportingInformation.getNotes());
    if (ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION.allowed(applicationVersion.getApplication().getType())) {
      summaryData.addKeyValue("ERAP alignment studies and projects", supportingInformation.getErapNotes());
    }

    var summaryCards = new ArrayList<SummaryCard>();

    summaryCards.add(SummaryCard.simpleSummaryCard(summaryData));
    getSupportingDocumentsSummaryCard(applicationVersion).ifPresent(summaryCards::add);

    return summaryCards;
  }

  Optional<SummaryCard> getSupportingDocumentsSummaryCard(ApplicationVersion applicationVersion) {
    var filesSummary = fieldConsentsFileService.getUploadedFiles(getFileUsage(applicationVersion))
        .stream()
        .map(uploadedFile -> SummaryFileView.from(uploadedFile, getDownloadUrl(applicationVersion, uploadedFile)))
        .toList();

    if (filesSummary.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(SummaryCard.filesSummaryCardWithHeading("Supporting information documents", filesSummary));
  }

  private FieldConsentsFileUsage getFileUsage(ApplicationVersion applicationVersion) {
    return ApplicationVersionFileUsage.supportingDocumentFrom(applicationVersion);
  }

  private String getDownloadUrl(ApplicationVersion applicationVersion, UploadedFile uploadedFile) {
    return ReverseRouter.route(on(SupportingInformationFileController.class)
        .download(applicationVersion.getId(), uploadedFile.getId(), null));
  }

}
