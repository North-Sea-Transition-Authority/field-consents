package uk.co.nstauthority.fieldconsents.application.consentlength;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;

@Service
public class ConsentLengthService {

  static final String INCORRECT_CONSENT_LENGTH_TYPE = "Incorrect consent length type: ";

  private final ConsentLengthRepository consentLengthRepository;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  public ConsentLengthService(ConsentLengthRepository consentLengthRepository,
                              ApplicationEventPublisher applicationEventPublisher) {
    this.consentLengthRepository = consentLengthRepository;
    this.applicationEventPublisher = applicationEventPublisher;
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
        default -> throw new RuntimeException(INCORRECT_CONSENT_LENGTH_TYPE + consentLengthType);
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

    ConsentLengthType consentLengthType = form.getConsentLengthType();
    consentLengthDetails.setConsentLength(consentLengthType);

    if (consentLengthDetailsOptional.isPresent()) {
      consentLengthRepository.deleteByApplicationVersion(currentVersion);
    }

    consentLengthDetails.setApplicationVersion(currentVersion);

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

      default -> throw new RuntimeException(INCORRECT_CONSENT_LENGTH_TYPE + consentLengthType);
    }
    consentLengthRepository.save(consentLengthDetails);
    applicationEventPublisher.publishEvent(new ConsentLengthChangeEvent(this, currentVersion.getId()));
  }

  public Optional<ConsentLengthDetails> findConsentLengthDetails(ApplicationVersion applicationVersion) {
    return consentLengthRepository.findByApplicationVersion(applicationVersion);
  }

  public ConsentLengthDetails getConsentLengthDetails(ApplicationVersion applicationVersion)
      throws EntityNotFoundException {
    return findConsentLengthDetails(applicationVersion)
        .orElseThrow(() -> new EntityNotFoundException("Consent details with application version id %s not found."
            .formatted(applicationVersion.getId())));
  }

  public SummaryCard getConsentLengthSummaryCard(ApplicationVersion applicationVersion) {
    var consentLengthDetailsOptional = findConsentLengthDetails(applicationVersion);

    if (consentLengthDetailsOptional.isEmpty()) {
      return SummaryCard.emptySummaryCard();
    }

    var consentLengthDetails = consentLengthDetailsOptional.get();

    var summaryData = SummaryDataView.newWithKeyValue("Consent period",
        consentLengthDetails.getConsentLength().getDisplayName());

    if (ConsentLengthType.SHORT_TERM.equals(consentLengthDetails.getConsentLength())) {
      summaryData
          .addKeyValue("Start date", consentLengthDetails.getShortTermStartDate())
          .addKeyValue("End date", consentLengthDetails.getShortTermEndDate());
    } else if (ConsentLengthType.ANNUAL.equals(consentLengthDetails.getConsentLength())) {
      summaryData.addKeyValue("Year", consentLengthDetails.getAnnualConsentYear());
    } else if (ConsentLengthType.LONG_TERM.equals(consentLengthDetails.getConsentLength())) {
      summaryData
          .addKeyValue("Start year", consentLengthDetails.getLongTermStartYear())
          .addKeyValue("End year", consentLengthDetails.getLongTermEndYear());
    }

    return SummaryCard.simpleSummaryCard(summaryData);
  }

  public LocalDate getProposedConsentStartDate(ConsentLengthDetails consentLengthDetails) {
    return switch (consentLengthDetails.getConsentLength()) {
      case SHORT_TERM -> consentLengthDetails.getShortTermStartDate();
      case ANNUAL -> LocalDate.of(consentLengthDetails.getAnnualConsentYear(), 1, 1);
      case LONG_TERM -> LocalDate.of(consentLengthDetails.getLongTermStartYear(), 1, 1);
    };
  }

  public LocalDate getProposedConsentEndDate(ConsentLengthDetails consentLengthDetails) {
    return switch (consentLengthDetails.getConsentLength()) {
      case SHORT_TERM -> consentLengthDetails.getShortTermEndDate();
      case ANNUAL -> LocalDate.of(consentLengthDetails.getAnnualConsentYear(), 12, 31);
      case LONG_TERM -> LocalDate.of(consentLengthDetails.getLongTermEndYear(), 12, 31);
    };
  }
}
