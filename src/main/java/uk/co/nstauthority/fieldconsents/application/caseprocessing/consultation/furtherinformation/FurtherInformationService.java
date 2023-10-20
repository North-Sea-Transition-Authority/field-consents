package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_FURTHER_INFORMATION_REQUESTED;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;

@Service
public class FurtherInformationService {

  private final Clock clock;
  private final FurtherInformationRepository repository;
  private final ApplicationWorkAreaPriorityService priorityService;
  private final EnergyPortalUserService energyPortalUserService;

  FurtherInformationService(
      Clock clock,
      FurtherInformationRepository repository,
      ApplicationWorkAreaPriorityService priorityService,
      EnergyPortalUserService energyPortalUserService
  ) {
    this.clock = clock;
    this.repository = repository;
    this.priorityService = priorityService;
    this.energyPortalUserService = energyPortalUserService;
  }

  public Optional<FurtherInformation> findLatestOpenFurtherInformation(Consultation consultation) {
    return repository.findByConsultationAndStatus(consultation, OPEN);
  }

  public List<FurtherInformation> getAllFurtherInformation(Collection<Consultation> consultations) {
    return repository.findAllByConsultationInOrderById(consultations);
  }

  @Transactional
  public void saveFurtherInformationRequest(Consultation consultation, ServiceUserDetail user, String requestText) {
    var furtherInformation = new FurtherInformation();
    furtherInformation.setStatus(OPEN);
    furtherInformation.setConsultation(consultation);
    furtherInformation.setRequestedAtDatetime(clock.instant());
    furtherInformation.setRequestedByWuaId(user.wuaId());
    furtherInformation.setRequestText(requestText);

    repository.save(furtherInformation);

    var applicationVersion = consultation.getRequestApplicationVersion();
    var reason = CONSULTATION_FURTHER_INFORMATION_REQUESTED;
    priorityService.prioritiseApplicationInWorkArea(applicationVersion, user, reason, REGULATOR);
    priorityService.prioritiseApplicationInWorkArea(applicationVersion, user, reason, CONSULTEE);
  }

  public FurtherInformationView getFurtherInformationView(FurtherInformation furtherInformation) {
    var requestedByWuaId = WebUserAccountId.from(furtherInformation.getRequestedByWuaId());
    var requestedByEnergyPortalUser = energyPortalUserService.getByWuaId(requestedByWuaId);

    return new FurtherInformationView(
        DateUtils.format(furtherInformation.getRequestedAtDatetime(), DATE_TIME),
        requestedByEnergyPortalUser.displayName(),
        furtherInformation.getRequestText()
    );
  }

}
