package uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTypeFeature;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.Consultation;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.response.ConsultationResponseFileController;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@Service
public class ConsultationSummaryService {

  private final ConsultationService consultationService;
  private final FurtherInformationService furtherInformationService;
  private final EnergyPortalUserService energyPortalUserService;
  private final FieldConsentsFileService fieldConsentsFileService;
  private static final Comparator<Consultation> CONSULTATION_COMPARATOR =
      Comparator.comparing(Consultation::getRequestedAtDatetime).reversed();

  ConsultationSummaryService(
      ConsultationService consultationService,
      FurtherInformationService furtherInformationService,
      EnergyPortalUserService energyPortalUserService,
      FieldConsentsFileService fieldConsentsFileService
  ) {
    this.consultationService = consultationService;
    this.furtherInformationService = furtherInformationService;
    this.energyPortalUserService = energyPortalUserService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  public List<SummaryItem> getConsultationSummaryItems(Application application) {
    var consultations = consultationService.getConsultationsByApplication(application)
        .stream()
        .sorted(CONSULTATION_COMPARATOR)
        .toList();

    if (consultations.isEmpty()) {
      return Collections.emptyList();
    }

    return getSummaryItems(consultations);
  }

  public List<SummaryItem> getConsultationSummaryItemsForUser(Application application, ServiceUserDetail user) {
    var consultations = consultationService.getConsultationsByApplicationForUser(application, user)
        .stream()
        .sorted(CONSULTATION_COMPARATOR)
        .toList();

    if (consultations.isEmpty()) {
      return Collections.emptyList();
    }

    return getSummaryItems(consultations);
  }

  List<SummaryItem> getSummaryItems(List<Consultation> consultations) {
    var furtherInformationByConsultationId = furtherInformationService.getAllFurtherInformation(consultations)
        .stream()
        .collect(Collectors.groupingBy(furtherInformation -> furtherInformation.getConsultation().getId()));

    var wuaIds = Stream.concat(
        consultations.stream()
            .flatMap(c -> Stream.of(c.getRequestedByWuaId(), c.getResponderWuaId(), c.getRespondedByWuaId())),
        furtherInformationByConsultationId.values().stream().flatMap(Collection::stream)
            .flatMap(fi -> Stream.of(fi.getRequestedByWuaId(), fi.getRespondedByWuaId())))
        .filter(Objects::nonNull)
        .map(WebUserAccountId::from)
        .collect(Collectors.toSet());

    var energyPortalUserByWuaId = energyPortalUserService.getEnergyPortalUserMap(wuaIds)
        .entrySet()
        .stream()
        .collect(Collectors.toMap(entry -> entry.getKey().id(), Map.Entry::getValue));

    if (energyPortalUserByWuaId.size() != wuaIds.size()) {
      throw new IllegalStateException("Fetched %d energy portal users but needed %d to complete successfully"
          .formatted(energyPortalUserByWuaId.size(), wuaIds.size()));
    }

    var summaryItems = new ArrayList<SummaryItem>();
    for (var i = 0; i < consultations.size(); i++) {
      var consultation = consultations.get(i);
      var furtherInformation = furtherInformationByConsultationId.getOrDefault(consultation.getId(), Collections.emptyList());
      var furtherInformationViews = furtherInformationService.getFurtherInformationViews(
          furtherInformation,
          energyPortalUserByWuaId
      );

      var summaryItem = getConsultationSummaryItem(
          consultations.size() - i,
          consultation,
          furtherInformationViews,
          energyPortalUserByWuaId
      );
      summaryItems.add(summaryItem);
    }

    return summaryItems;
  }

  SummaryItem getConsultationSummaryItem(
      int index,
      Consultation consultation,
      List<FurtherInformationView> furtherInformationViews,
      Map<Long, EnergyPortalUserDto> energyPortalUserByWuaId
  ) {
    var cards = new ArrayList<SummaryCard>();

    cards.add(getConsultationSummaryCard(consultation, energyPortalUserByWuaId));
    getFilesSummaryCard(consultation).ifPresent(cards::add);

    for (var i = 0; i < furtherInformationViews.size(); i++) {
      var heading = "Further information request %s".formatted(furtherInformationViews.size() - i);
      var card = furtherInformationViews.get(i).toSummaryCardWithHeading(heading);
      cards.add(card);
    }

    var heading = "Consultation %s".formatted(index);
    return SummaryItem.withCards(heading, cards);
  }

  SummaryCard getConsultationSummaryCard(Consultation consultation, Map<Long, EnergyPortalUserDto> energyPortalUserByWuaId) {
    // This can be null because the consultation may not be allocated to a responder yet
    var responder = Optional.ofNullable(consultation.getResponderWuaId())
        .map(energyPortalUserByWuaId::get)
        .map(EnergyPortalUserDto::displayName)
        .orElse("");

    var summaryDataView = SummaryDataView
        .newWithKeyValue("Consultation status", consultation.getStatus().getDisplayName())
        .addKeyValue("Deadline", DateUtils.format(consultation.getRequestDeadline(), DateUtils.DATE_TIME))
        .addKeyValue("Consultee", consultation.getConsultationTeam().getDisplayName())
        .addKeyValue("Responder", responder)
        .addKeyValue("Request application version", consultation.getRequestApplicationVersion().getVersion())
        .addKeyValue("Requested by", energyPortalUserByWuaId.get(consultation.getRequestedByWuaId()).displayName())
        .addKeyValue("Requested on", DateUtils.format(consultation.getRequestedAtDatetime(), DateUtils.DATE_TIME));

    if (!ConsultationStatus.CLOSED.equals(consultation.getStatus())) {
      return SummaryCard.simpleSummaryCard(summaryDataView);
    }

    summaryDataView
        .addKeyValue("Responded by", energyPortalUserByWuaId.get(consultation.getRespondedByWuaId()).displayName())
        .addKeyValue("Responded on", DateUtils.format(consultation.getRespondedAtDatetime(), DateUtils.DATE_TIME))
        .addKeyValue("Habitats regulations response", consultation.getHabitatsRegsResponseType().getDisplayName())
        .addKeyValue("Habitats regulations response description", consultation.getHabitatsRegsResponseDescription());

    var applicationType = consultation.getRequestApplicationVersion().getApplication().getType();
    if (ApplicationTypeFeature.EIA_SCREENING_DIRECTION.allowed(applicationType)) {
      summaryDataView
          .addKeyValue("EIA regulations response", consultation.getEiaRegsResponseType().getDisplayName())
          .addKeyValue("EIA regulations response description", consultation.getEiaRegsResponseDescription());
    }

    return SummaryCard.simpleSummaryCard(summaryDataView);
  }

  Optional<SummaryCard> getFilesSummaryCard(Consultation consultation) {
    var applicationId = consultation.getRequestApplicationVersion().getApplication().getId();
    var consultationId = consultation.getId();
    var fileSummaries = fieldConsentsFileService.getUploadedFiles(ConsultationFileUsage.responseUsageFrom(consultation))
        .stream()
        .map(uploadedFile -> SummaryFileView.from(
            uploadedFile,
            ReverseRouter.route(on(ConsultationResponseFileController.class).download(
                applicationId,
                consultationId,
                uploadedFile.getId(),
                null
            ))
        ))
        .toList();

    if (fileSummaries.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(SummaryCard.filesSummaryCardWithHeading("Response documents", fileSummaries));
  }

}
