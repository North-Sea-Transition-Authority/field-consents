package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.FURTHER_INFORMATION_REQUEST_OPENED;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;

@Service
public class FurtherInformationRequestService {

  private final Clock clock;
  private final FurtherInformationRequestRepository repository;
  private final ApplicationWorkAreaPriorityService priorityService;

  FurtherInformationRequestService(
      Clock clock,
      FurtherInformationRequestRepository repository,
      ApplicationWorkAreaPriorityService priorityService
  ) {
    this.clock = clock;
    this.repository = repository;
    this.priorityService = priorityService;
  }

  public Optional<FurtherInformationRequest> findLatestOpenFurtherInformationRequest(Consultation consultation) {
    return repository.findByConsultationAndStatus(consultation, OPEN);
  }

  public List<FurtherInformationRequest> getAllFurtherInformationRequests(Collection<Consultation> consultations) {
    return repository.findAllByConsultationInOrderById(consultations);
  }

  @Transactional
  public void saveFurtherInformationRequest(Consultation consultation, ServiceUserDetail user, String requestText) {
    var furtherInformationRequest = new FurtherInformationRequest();
    furtherInformationRequest.setStatus(OPEN);
    furtherInformationRequest.setConsultation(consultation);
    furtherInformationRequest.setRequestedAtDatetime(clock.instant());
    furtherInformationRequest.setRequestedByWuaId(user.wuaId());
    furtherInformationRequest.setRequestText(requestText);

    repository.save(furtherInformationRequest);

    var applicationVersion = consultation.getRequestApplicationVersion();
    priorityService.prioritiseApplicationInWorkArea(applicationVersion, user, FURTHER_INFORMATION_REQUEST_OPENED, REGULATOR);
    priorityService.prioritiseApplicationInWorkArea(applicationVersion, user, FURTHER_INFORMATION_REQUEST_OPENED, CONSULTEE);
  }

}
