package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.INDUSTRY;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.OPERATOR_WITHDRAWAL_REQUEST;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.REGULATOR_REJECT_WITHDRAWAL_REQUEST;

import java.time.Clock;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersionService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class ApplicationWithdrawalService {

  static final String NO_OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID =
      "There is no open withdrawal request for the application with id %s";

  static final String OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID =
      "A withdrawal request has already been submitted for the application with id %s";

  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationWithdrawalService.class);

  private final Clock clock;

  private final ApplicationVersionService applicationVersionService;

  private final ApplicationWithdrawalRepository applicationWithdrawalRepository;

  private final ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  private final ApplicationWithdrawalEmailService applicationWithdrawalEmailService;

  public ApplicationWithdrawalService(Clock clock,
                                      ApplicationVersionService applicationVersionService,
                                      ApplicationWithdrawalRepository applicationWithdrawalRepository,
                                      ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService,
                                      ApplicationWithdrawalEmailService applicationWithdrawalEmailService) {
    this.clock = clock;
    this.applicationVersionService = applicationVersionService;
    this.applicationWithdrawalRepository = applicationWithdrawalRepository;
    this.applicationWorkAreaPriorityService = applicationWorkAreaPriorityService;
    this.applicationWithdrawalEmailService = applicationWithdrawalEmailService;
  }

  public boolean openWithdrawalExists(ApplicationVersion applicationVersion) {
    return applicationWithdrawalRepository
        .existsByApplicationVersion_ApplicationAndWithdrawalStatus(applicationVersion.getApplication(), WithdrawalStatus.OPEN);
  }

  public WithdrawalRequestForm getWithdrawalRequestForm(ApplicationVersion applicationVersion) {
    if (openWithdrawalExists(applicationVersion)) {
      throw new IllegalStateException(
          String.format(OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID,
              applicationVersion.getApplication().getId())
      );
    }
    return new WithdrawalRequestForm();
  }

  public void saveWithdrawalRequest(ApplicationVersion applicationVersion,
                                    String requestText,
                                    ServiceUserDetail user) {
    var applicationWithdrawal = new ApplicationWithdrawal();
    applicationWithdrawal.setApplicationVersion(applicationVersion);
    applicationWithdrawal.setWithdrawalStatus(WithdrawalStatus.OPEN);
    applicationWithdrawal.setRequestText(requestText);
    applicationWithdrawal.setRequestedByWuaId(user.wuaId());
    applicationWithdrawal.setRequestedDateTime(clock.instant());
    applicationWithdrawalRepository.save(applicationWithdrawal);
    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(applicationVersion, user,
        OPERATOR_WITHDRAWAL_REQUEST, INDUSTRY);
    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(applicationVersion, user,
        OPERATOR_WITHDRAWAL_REQUEST, REGULATOR);

    try {
      applicationWithdrawalEmailService.sendApplicationWithdrawalRequestEmail(applicationWithdrawal);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send an application withdrawal request notification \
              by user with wuaId [{}] for application version with id [{}] failed. \
              Note: this hasn't prevented the application withdrawal request being saved.
              """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
  }

  public Optional<ApplicationWithdrawal> findOpenApplicationWithdrawal(ApplicationVersion applicationVersion) {
    return applicationWithdrawalRepository
        .findByApplicationVersion_ApplicationAndWithdrawalStatus(applicationVersion.getApplication(), WithdrawalStatus.OPEN);
  }

  public ApplicationWithdrawal getOpenApplicationWithdrawal(ApplicationVersion applicationVersion) {
    return findOpenApplicationWithdrawal(applicationVersion)
        .orElseThrow(() ->
            new IllegalStateException(
                String.format(NO_OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID, applicationVersion.getApplication().getId()))
        );
  }

  public List<ApplicationWithdrawal> getApplicationWithdrawalsByApplication(Application application) {
    return applicationWithdrawalRepository.findByApplicationVersion_Application(application);
  }

  public WithdrawalResponseForm getWithdrawalResponseForm(ApplicationVersion applicationVersion) {
    if (findOpenApplicationWithdrawal(applicationVersion).isEmpty()) {
      throw new IllegalStateException(
          String.format(NO_OPEN_WITHDRAWAL_FOUND_FOR_APPLICATION_WITH_ID, applicationVersion.getApplication().getId()));
    }
    return new WithdrawalResponseForm();
  }

  public void saveWithdrawalResponse(ApplicationVersion applicationVersion,
                                     WithdrawalStatus responseStatus,
                                     String responseText,
                                     ServiceUserDetail user) {
    var applicationWithdrawal = getOpenApplicationWithdrawal(applicationVersion);
    applicationWithdrawal.setWithdrawalStatus(responseStatus);
    applicationWithdrawal.setResponseText(responseText);
    applicationWithdrawal.setRespondedByWuaId(user.wuaId());
    applicationWithdrawal.setRespondedDateTime(clock.instant());
    applicationWithdrawalRepository.save(applicationWithdrawal);
    if (WithdrawalStatus.ACCEPTED.equals(responseStatus)) {
      applicationVersionService.withdrawApplicationVersion(applicationVersion);
    } else {
      applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
          applicationVersion,
          user,
          REGULATOR_REJECT_WITHDRAWAL_REQUEST, INDUSTRY
      );
    }

    try {
      applicationWithdrawalEmailService.sendApplicationWithdrawalResponseEmail(applicationWithdrawal);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send an application withdrawal response notification \
              by user with wuaId [{}] for application version with id [{}] failed. \
              Note: this hasn't prevented the application withdrawal response being saved.
              """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
  }
}
