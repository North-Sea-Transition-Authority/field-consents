package uk.co.nstauthority.fieldconsents.application.caseprocessing.withdrawal;

import java.time.Clock;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class ApplicationWithdrawalService {

  private final Clock clock;

  private final ApplicationWithdrawalRepository applicationWithdrawalRepository;

  public ApplicationWithdrawalService(Clock clock,
                                      ApplicationWithdrawalRepository applicationWithdrawalRepository) {
    this.clock = clock;
    this.applicationWithdrawalRepository = applicationWithdrawalRepository;
  }

  public boolean openWithdrawalExists(ApplicationVersion applicationVersion) {
    return applicationWithdrawalRepository
        .existsByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN);
  }

  public WithdrawalRequestForm getWithdrawalRequestForm(ApplicationVersion applicationVersion) {
    if (applicationWithdrawalRepository.findByApplicationVersionAndWithdrawalStatus(applicationVersion, WithdrawalStatus.OPEN)
        .isPresent()) {
      throw new IllegalStateException(
          String.format("A withdrawal request has already been submitted for the application with id %s",
              applicationVersion.getApplication().getId())
      );
    }
    return new WithdrawalRequestForm();
  }

  public void saveWithdrawalRequest(ApplicationVersion applicationVersion,
                                    WithdrawalRequestForm form,
                                    ServiceUserDetail user) {
    var applicationWithdrawal = new ApplicationWithdrawal();
    applicationWithdrawal.setApplicationVersion(applicationVersion);
    applicationWithdrawal.setWithdrawalStatus(WithdrawalStatus.OPEN);
    applicationWithdrawal.setRequestText(form.getRequestText().getInputValue());
    applicationWithdrawal.setRequestedByWuaId(user.wuaId());
    applicationWithdrawal.setRequestedDateTime(clock.instant());
    applicationWithdrawalRepository.save(applicationWithdrawal);
  }
}
