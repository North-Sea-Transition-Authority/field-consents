package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@Service
public class SupportingInformationService {

  private final SupportingInformationRepository supportingInformationRepository;

  @Autowired
  public SupportingInformationService(SupportingInformationRepository supportingInformationRepository) {
    this.supportingInformationRepository = supportingInformationRepository;
  }

  public SupportingInformationForm getSupportingInformationForm(ApplicationVersion applicationVersion) {
    return supportingInformationRepository.findByApplicationVersion(applicationVersion)
        .map(SupportingInformationForm::from)
        .orElseGet(SupportingInformationForm::new);
  }

  public Optional<SupportingInformation> findSupportingInformation(ApplicationVersion applicationVersion) {
    return supportingInformationRepository.findByApplicationVersion(applicationVersion);
  }

  @Transactional
  public void saveSupportingInformation(ApplicationVersion applicationVersion,
                                        SupportingInformationForm form) {
    supportingInformationRepository.deleteByApplicationVersion(applicationVersion);
    supportingInformationRepository.save(SupportingInformation.from(applicationVersion, form));
  }

  public SummaryCard getSupportingInformationSummaryCard(ApplicationVersion applicationVersion) {
    var supportingInformationOptional = findSupportingInformation(applicationVersion);

    if (supportingInformationOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    List<SummaryKeyValue> summaryKeyValues = new ArrayList<>();
    var supportingInformation = supportingInformationOptional.get();

    summaryKeyValues.add(SummaryKeyValue.from("Notes", supportingInformation.getNotes()));

    if (ApplicationTypeFeature.ERAP_SUPPORTING_INFORMATION.allowed(applicationVersion.getApplication().getType())) {
      summaryKeyValues.add(SummaryKeyValue.from("ERAP alignment studies and projects", supportingInformation.getErapNotes()));
    }

    return SummaryCard.simpleSummaryCard(summaryKeyValues);
  }
}
