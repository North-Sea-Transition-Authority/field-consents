package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation;

import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus.CLOSED;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationStatus.OPEN;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.CONSULTEE;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityGroup.REGULATOR;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_FURTHER_INFORMATION_REQUESTED;
import static uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityReason.CONSULTATION_FURTHER_INFORMATION_RESPONDED;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.workareapriority.ApplicationWorkAreaPriorityService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@Service
public class FurtherInformationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(FurtherInformationService.class);

  private final Clock clock;
  private final FurtherInformationRepository repository;
  private final ApplicationWorkAreaPriorityService priorityService;
  private final EnergyPortalUserService energyPortalUserService;
  private final FurtherInformationEmailService furtherInformationEmailService;
  private final ConsultationService consultationService;

  FurtherInformationService(
      Clock clock,
      FurtherInformationRepository repository,
      ApplicationWorkAreaPriorityService priorityService,
      EnergyPortalUserService energyPortalUserService,
      FurtherInformationEmailService furtherInformationEmailService,
      ConsultationService consultationService
  ) {
    this.clock = clock;
    this.repository = repository;
    this.priorityService = priorityService;
    this.energyPortalUserService = energyPortalUserService;
    this.furtherInformationEmailService = furtherInformationEmailService;
    this.consultationService = consultationService;
  }

  public Optional<FurtherInformation> findLatestOpenFurtherInformation(Consultation consultation) {
    return repository.findByConsultationAndStatus(consultation, OPEN);
  }

  public List<FurtherInformation> getAllFurtherInformation(Collection<Consultation> consultations) {
    return repository.findAllByConsultationInOrderById(consultations);
  }

  public FurtherInformation getLatestOpenFurtherInformation(Consultation consultation) {
    return findLatestOpenFurtherInformation(consultation)
        .orElseThrow(() -> new EntityNotFoundException(
            "Open further information not found for consultation [%s]".formatted(consultation.getId())));
  }

  public boolean isFurtherInformationRequestOpen(ApplicationVersion applicationVersion) {
    return consultationService.findLatestOpenConsultation(applicationVersion.getApplication())
        .flatMap(this::findLatestOpenFurtherInformation)
        .isPresent();
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

    try {
      furtherInformationEmailService.sendFurtherInformationRequestEmail(furtherInformation);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a further information request notification by user with wuaId [{}] for application \
              version with id [{}] failed. \
              Note: this hasn't prevented the further information request being submitted.
              """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
  }

  @Transactional
  public void saveFurtherInformationResponse(FurtherInformation furtherInformation, ServiceUserDetail user, String responseText) {
    if (!OPEN.equals(furtherInformation.getStatus())) {
      throw new IllegalArgumentException("Expected further information [%s] to be OPEN when saving response"
          .formatted(furtherInformation.getId()));
    }

    furtherInformation.setStatus(CLOSED);
    furtherInformation.setRespondedAtDatetime(clock.instant());
    furtherInformation.setRespondedByWuaId(user.wuaId());
    furtherInformation.setResponseText(responseText);

    repository.save(furtherInformation);

    var applicationVersion = furtherInformation.getConsultation().getRequestApplicationVersion();
    var reason = CONSULTATION_FURTHER_INFORMATION_RESPONDED;
    priorityService.prioritiseApplicationInWorkArea(applicationVersion, user, reason, REGULATOR);
    priorityService.prioritiseApplicationInWorkArea(applicationVersion, user, reason, CONSULTEE);

    try {
      furtherInformationEmailService.sendFurtherInformationResponseEmail(furtherInformation);
    } catch (Exception exception) {
      LOGGER.error("""
              An attempt to send a further information response notification by user with wuaId [{}] for application \
              version with id [{}] failed. \
              Note: this hasn't prevented the further information response being submitted.
              """,
          user.wuaId(), applicationVersion.getId(), exception);
    }
  }

  public FurtherInformationView getFurtherInformationView(FurtherInformation furtherInformation) {
    return getFurtherInformationViews(Collections.singleton(furtherInformation)).get(0);
  }

  public List<FurtherInformationView> getFurtherInformationViews(Collection<FurtherInformation> furtherInformation) {
    var wuaIds = furtherInformation.stream()
        .flatMap(fi -> Stream.of(fi.getRequestedByWuaId(), fi.getRespondedByWuaId()))
        .filter(Objects::nonNull)
        .map(WebUserAccountId::from)
        .collect(Collectors.toSet());

    var energyPortalUserByWuaId = energyPortalUserService.getEnergyPortalUserMap(wuaIds)
        .entrySet()
        .stream()
        .collect(Collectors.toMap(
            entry -> entry.getKey().id(),
            Map.Entry::getValue
        ));

    return getFurtherInformationViews(furtherInformation, energyPortalUserByWuaId);
  }

  public List<FurtherInformationView> getFurtherInformationViews(
      Collection<FurtherInformation> furtherInformation,
      Map<Long, EnergyPortalUserDto> energyPortalUserByWuaId
  ) {
    return furtherInformation.stream()
        .sorted(Comparator.comparing(FurtherInformation::getRequestedAtDatetime).reversed())
        .map(fi -> FurtherInformationView.newBuilder()
            .withRequestedByUser(energyPortalUserByWuaId.get(fi.getRequestedByWuaId()))
            .withRequestedAtTimestamp(fi.getRequestedAtDatetime())
            .withRequestText(fi.getRequestText())
            .withStatus(fi.getStatus())
            .withRespondedByUser(Optional.ofNullable(fi.getRespondedByWuaId())
                .map(energyPortalUserByWuaId::get)
                .orElse(null))
            .withRespondedAtTimestamp(fi.getRespondedAtDatetime())
            .withResponseText(fi.getResponseText())
            .build())
        .toList();
  }

}
