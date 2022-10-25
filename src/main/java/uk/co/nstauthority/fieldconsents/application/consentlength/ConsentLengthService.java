package uk.co.nstauthority.fieldconsents.application.consentlength;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;

@Service
public class ConsentLengthService {

  private final ConsentLengthRepository consentLengthRepository;

  @Autowired
  public ConsentLengthService(ConsentLengthRepository consentLengthRepository) {
    this.consentLengthRepository = consentLengthRepository;
  }

  public ConsentLengthForm getConsentLengthForm(ApplicationVersion currentVersion) {
    ConsentLengthForm form = new ConsentLengthForm();
    Optional<ConsentLengthDetails> consentLengthDetailsOptional =
        consentLengthRepository.findByApplicationVersion(currentVersion);
    if (consentLengthDetailsOptional.isPresent()) {
      ConsentLengthDetails consentLengthDetails = consentLengthDetailsOptional.get();
      ConsentLengthType consentLengthType = consentLengthDetails.getConsentLength();
      form.setConsentLengthType(consentLengthType);

      switch (consentLengthType) {
        case SHORT_TERM -> populateShortTermOnForm(form, consentLengthDetails);
        case ANNUAL -> form.getAnnualConsentYear().setInteger(consentLengthDetails.getAnnualConsentYear());
        case LONG_TERM -> populateLongTermOnForm(form, consentLengthDetails);
        default -> throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
      }
    }
    return form;
  }

  private void populateLongTermOnForm(ConsentLengthForm form, ConsentLengthDetails consentLengthDetails) {
    form.getLongTermStartYear().setInteger(consentLengthDetails.getLongTermStartYear());
    form.getLongTermEndYear().setInteger(consentLengthDetails.getLongTermEndYear());
  }

  private void populateShortTermOnForm(ConsentLengthForm form, ConsentLengthDetails consentLengthDetails) {
    LocalDate shortTermStartDate = LocalDate.of(
        consentLengthDetails.getShortTermStartDate().getYear(),
        consentLengthDetails.getShortTermStartDate().getMonth(),
        consentLengthDetails.getShortTermStartDate().getDayOfMonth()
    );
    form.getShortTermStartDate().setDate(shortTermStartDate);


    LocalDate shortTermEndDate = LocalDate.of(
        consentLengthDetails.getShortTermEndDate().getYear(),
        consentLengthDetails.getShortTermEndDate().getMonth(),
        consentLengthDetails.getShortTermEndDate().getDayOfMonth()
    );
    form.getShortTermEndDate().setDate(shortTermEndDate);
  }

  @Transactional
  public void saveConsentLengthDetails(ApplicationVersion currentVersion, ConsentLengthForm form) {
    ConsentLengthDetails consentLengthDetails = new ConsentLengthDetails();
    Optional<ConsentLengthDetails> consentLengthDetailsOptional =
        consentLengthRepository.findByApplicationVersion(currentVersion);
    if (consentLengthDetailsOptional.isPresent()) {
      // TODO If this form seats on the same task-list as the application forms (short, annual, long) you will also need
      //      to delete any existing record of a pre-saved term details which are obsolete
      //      FCS-236 : Delete old short, annual or long term production data when consent length application changes
      consentLengthRepository.deleteByApplicationVersion(currentVersion);
    }

    consentLengthDetails.setApplicationVersion(currentVersion);
    ConsentLengthType consentLengthType = form.getConsentLengthType();
    consentLengthDetails.setConsentLength(consentLengthType);

    switch (consentLengthType) {
      case SHORT_TERM -> {
        consentLengthDetails.setShortTermStartDate(
            form.getShortTermStartDate().getAsLocalDate().orElseThrow());
        consentLengthDetails.setShortTermEndDate(
            form.getShortTermEndDate().getAsLocalDate().orElseThrow());
      }
      case ANNUAL -> consentLengthDetails.setAnnualConsentYear(
          form.getAnnualConsentYear().getAsInteger().orElseThrow(NoSuchElementException::new)
      );
      case LONG_TERM -> {
        consentLengthDetails.setLongTermStartYear(
            form.getLongTermStartYear().getAsInteger().orElseThrow(NoSuchElementException::new)
        );
        consentLengthDetails.setLongTermEndYear(
            form.getLongTermEndYear().getAsInteger().orElseThrow(NoSuchElementException::new)
        );
      }

      default -> throw new RuntimeException("Incorrect consent length type: " + consentLengthType);
    }
    consentLengthRepository.save(consentLengthDetails);
  }
}
