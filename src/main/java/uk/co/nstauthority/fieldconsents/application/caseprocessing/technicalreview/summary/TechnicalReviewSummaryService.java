package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.summary;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.document.TechnicalReviewFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.document.TechnicalReviewResponseDocumentController;
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
public class TechnicalReviewSummaryService {

  private final TechnicalReviewService technicalReviewService;
  private final EnergyPortalUserService energyPortalUserService;
  private final FieldConsentsFileService fieldConsentsFileService;

  TechnicalReviewSummaryService(TechnicalReviewService technicalReviewService,
                                EnergyPortalUserService energyPortalUserService,
                                FieldConsentsFileService fieldConsentsFileService) {
    this.technicalReviewService = technicalReviewService;
    this.energyPortalUserService = energyPortalUserService;
    this.fieldConsentsFileService = fieldConsentsFileService;
  }

  public List<SummaryItem> getTechnicalReviewSummaryItems(Application application) {
    var technicalReviews = technicalReviewService.getTechnicalReviewsByApplication(application)
        .stream()
        .sorted(Comparator.comparing(TechnicalReview::getRequestedDateTime).reversed())
        .toList();

    if (technicalReviews.isEmpty()) {
      return Collections.emptyList();
    }

    var energyPortalUserMap = getEnergyPortalUserMap(technicalReviews);

    var technicalReviewCount = technicalReviews.size();

    return IntStream.range(0, technicalReviewCount)
        .mapToObj(index -> getTechnicalReviewSummaryItem(
            technicalReviews.get(index),
            "Technical review %s".formatted(technicalReviewCount - index),
            energyPortalUserMap
        ))
        .toList();
  }

  private SummaryItem getTechnicalReviewSummaryItem(TechnicalReview technicalReview,
                                                    String displayName,
                                                    Map<WebUserAccountId, EnergyPortalUserDto> energyPortalUserMap) {

    var summaryCards = new ArrayList<SummaryCard>();
    summaryCards.add(getTechnicalReviewDetailsSummaryCard(technicalReview, energyPortalUserMap));

    var summaryFileViews = getSummaryFileViews(technicalReview);
    if (!summaryFileViews.isEmpty()) {
      summaryCards.add(SummaryCard.filesSummaryCardWithHeading("Response documents", summaryFileViews));
    }

    return SummaryItem.withCards(displayName, summaryCards);
  }

  private SummaryCard getTechnicalReviewDetailsSummaryCard(TechnicalReview technicalReview,
                                                           Map<WebUserAccountId, EnergyPortalUserDto> energyPortalUserMap) {

    var summaryData = SummaryDataView
        .newWithKeyValue("Review status", technicalReview.getTechnicalReviewStatus().getDisplayName())
        .addKeyValue("Request application version", technicalReview.getRequestApplicationVersion().getVersion().toString())
        .addKeyValue("Requested by",
            energyPortalUserMap.get(WebUserAccountId.from(technicalReview.getRequestedByWuaId())).displayName())
        .addKeyValue("Requested on", DateUtils.format(technicalReview.getRequestedDateTime(), DATE_TIME))
        .addKeyValue("Notes for the reviewer", technicalReview.getRequestText())
        .addKeyValue("Deadline", DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        .addKeyValue("Technical reviewer",
            energyPortalUserMap.get(WebUserAccountId.from(technicalReview.getTechnicalReviewerWuaId())).displayName());

    if (TechnicalReviewStatus.CLOSED.equals(technicalReview.getTechnicalReviewStatus())) {
      summaryData
          .addKeyValue("Response application version", technicalReview.getResponseApplicationVersion().getVersion().toString())
          .addKeyValue("Responded by",
              energyPortalUserMap.get(WebUserAccountId.from(technicalReview.getRespondedByWuaId())).displayName())
          .addKeyValue("Responded on", DateUtils.format(technicalReview.getRespondedDateTime(), DATE_TIME))
          .addKeyValue("Decision", technicalReview.getResponseType().getDisplayName())
          .addKeyValue(technicalReview.getResponseType().getResponseTextLabel(), technicalReview.getResponseText());
    }

    return SummaryCard.simpleSummaryCard(summaryData);
  }

  private Map<WebUserAccountId, EnergyPortalUserDto> getEnergyPortalUserMap(List<TechnicalReview> technicalReviews) {
    var webUserAccountIds = technicalReviews
        .stream()
        .flatMap(technicalReview -> Stream.of(
            technicalReview.getRequestedByWuaId(),
            technicalReview.getTechnicalReviewerWuaId(),
            technicalReview.getRespondedByWuaId()
        ))
        .filter(Objects::nonNull)
        .distinct()
        .map(WebUserAccountId::from)
        .toList();

    return energyPortalUserService.getEnergyPortalUserMap(webUserAccountIds);
  }

  private List<SummaryFileView> getSummaryFileViews(TechnicalReview technicalReview) {
    var applicationId = technicalReview.getRequestApplicationVersion().getApplication().getId();
    return fieldConsentsFileService
        .getUploadedFiles(TechnicalReviewFileUsage.responseFrom(technicalReview))
        .stream()
        .map(uploadedFile -> this.getSummaryFileView(uploadedFile, applicationId, technicalReview.getId()))
        .toList();
  }

  private SummaryFileView getSummaryFileView(UploadedFile uploadedFile, Integer applicationId, Integer technicalReviewId) {
    return SummaryFileView.from(
        uploadedFile,
        ReverseRouter.route(on(TechnicalReviewResponseDocumentController.class)
            .download(applicationId, technicalReviewId, uploadedFile.getId()))
    );
  }
}
