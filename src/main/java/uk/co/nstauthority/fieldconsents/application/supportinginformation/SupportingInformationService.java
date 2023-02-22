package uk.co.nstauthority.fieldconsents.application.supportinginformation;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

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
}
