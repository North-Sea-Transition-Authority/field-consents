package uk.co.nstauthority.fieldconsents.application.workareapriority;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class ApplicationWorkAreaPriorityService {

  private final ApplicationWorkAreaPriorityRepository applicationWorkAreaPriorityRepository;

  private final Clock clock;

  @Autowired
  ApplicationWorkAreaPriorityService(ApplicationWorkAreaPriorityRepository applicationWorkAreaPriorityRepository,
                                     Clock clock) {
    this.applicationWorkAreaPriorityRepository = applicationWorkAreaPriorityRepository;
    this.clock = clock;
  }

  @Transactional
  public void prioritiseApplicationInWorkArea(ApplicationVersion applicationVersion,
                                              ServiceUserDetail user,
                                              ApplicationWorkAreaPriorityReason applicationWorkAreaPriorityReason,
                                              ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup) {

    var applicationWorkAreaPriorityOptional = applicationWorkAreaPriorityRepository
        .findByApplicationVersionAndWorkAreaPriorityGroup(
            applicationVersion,
            applicationWorkAreaPriorityGroup
        );

    ApplicationWorkAreaPriority applicationWorkAreaPriority;
    if (applicationWorkAreaPriorityOptional.isEmpty()) {
      applicationWorkAreaPriority = new ApplicationWorkAreaPriority(
          applicationVersion,
          applicationWorkAreaPriorityGroup,
          applicationWorkAreaPriorityReason,
          clock.instant(),
          user.wuaId()
      );
    } else {
      applicationWorkAreaPriority = applicationWorkAreaPriorityOptional.get();
      applicationWorkAreaPriority.setWorkAreaPriorityReason(applicationWorkAreaPriorityReason);
      applicationWorkAreaPriority.setWorkAreaPriorityDateTime(clock.instant());
      applicationWorkAreaPriority.setWorkAreaPriorityByWuaId(user.wuaId());
    }

    applicationWorkAreaPriorityRepository.save(applicationWorkAreaPriority);
  }
}
