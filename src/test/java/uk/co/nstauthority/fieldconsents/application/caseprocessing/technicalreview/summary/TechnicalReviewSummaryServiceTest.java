package uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.summary;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_EPU;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.CASE_OFFICER_USER;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_EPU_1;
import static uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil.TECHNICAL_REVIEWER_USER_1;
import static uk.co.nstauthority.fieldconsents.file.FieldConsentsFileTestUtil.createUploadedFile;
import static uk.co.nstauthority.fieldconsents.formatting.DateUtils.DATE_TIME;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.fileuploadlibrary.core.UploadedFile;
import uk.co.nstauthority.fieldconsents.application.Application;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReview;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewResponseType;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewStatus;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.TechnicalReviewFileUsage;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.response.TechnicalReviewResponseFileController;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileUsage;
import uk.co.nstauthority.fieldconsents.formatting.DateUtils;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryDataView;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;
import uk.co.nstauthority.fieldconsents.summary.SummaryItem;

@ExtendWith(MockitoExtension.class)
class TechnicalReviewSummaryServiceTest {

  @Mock
  private TechnicalReviewService technicalReviewService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @InjectMocks
  private TechnicalReviewSummaryService technicalReviewSummaryService;

  private Application application;

  private static final int TECHNICAL_REVIEW1_ID = 1;
  private TechnicalReview technicalReview1;
  private static final int TECHNICAL_REVIEW2_ID = 2;
  private TechnicalReview technicalReview2;
  private static final int TECHNICAL_REVIEW3_ID = 3;
  private TechnicalReview technicalReview3;
  private static final int TECHNICAL_REVIEW4_ID = 4;
  private TechnicalReview technicalReview4Open;
  private List<TechnicalReview> technicalReviews;
  private FieldConsentsFileUsage technicalReview1FileUsage;
  private List<UploadedFile> technicalReview1UploadedFiles;

  @BeforeEach
  void setUp() {
    var applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
    application = applicationVersion.getApplication();
    technicalReview1 = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion, TechnicalReviewResponseType.APPROVE);
    technicalReview1.setId(TECHNICAL_REVIEW1_ID);
    technicalReview2 = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion, TechnicalReviewResponseType.REJECT);
    technicalReview2.setId(TECHNICAL_REVIEW2_ID);
    technicalReview3 = TechnicalReviewTestUtil.getClosedTechnicalReviewWithResponseType(
        applicationVersion, null);
    technicalReview3.setId(TECHNICAL_REVIEW3_ID);
    technicalReview4Open = TechnicalReviewTestUtil.getOpenTechnicalReview(applicationVersion);
    technicalReview4Open.setId(TECHNICAL_REVIEW4_ID);
    technicalReviews = List.of(technicalReview4Open, technicalReview3, technicalReview2, technicalReview1);

    technicalReview1FileUsage = TechnicalReviewFileUsage.responseFrom(technicalReview1);
    technicalReview1UploadedFiles = new ArrayList<>();
    technicalReview1UploadedFiles.add(createUploadedFile(technicalReview1FileUsage));
  }

  @Test
  void getTechnicalReviewSummaryItems_whenNoTechnicalReviews() {
    when(technicalReviewService.getTechnicalReviewsByApplication(application))
        .thenReturn(Collections.emptyList());

    assertThat(technicalReviewSummaryService.getTechnicalReviewSummaryItems(application))
        .isEmpty();
  }

  @Test
  void getTechnicalReviewSummaryItems_whenTechnicalReviewsExist() {
    when(technicalReviewService.getTechnicalReviewsByApplication(application))
        .thenReturn(technicalReviews);

    var webUserAccountIds =
        List.of(WebUserAccountId.from(CASE_OFFICER_USER.wuaId()), WebUserAccountId.from(TECHNICAL_REVIEWER_USER_1.wuaId()));
    var energyPortalUserMap = Map.of(
        WebUserAccountId.from(CASE_OFFICER_EPU.webUserAccountId()),CASE_OFFICER_EPU,
        WebUserAccountId.from(TECHNICAL_REVIEWER_EPU_1.webUserAccountId()), TECHNICAL_REVIEWER_EPU_1);
    when(energyPortalUserService.getEnergyPortalUserMap(webUserAccountIds))
        .thenReturn(energyPortalUserMap);

    when(fieldConsentsFileService.getUploadedFiles(TechnicalReviewFileUsage.responseFrom(technicalReview4Open)))
        .thenReturn(Collections.emptyList());
    when(fieldConsentsFileService.getUploadedFiles(TechnicalReviewFileUsage.responseFrom(technicalReview3)))
        .thenReturn(Collections.emptyList());
    when(fieldConsentsFileService.getUploadedFiles(TechnicalReviewFileUsage.responseFrom(technicalReview2)))
        .thenReturn(Collections.emptyList());
    when(fieldConsentsFileService.getUploadedFiles(technicalReview1FileUsage))
        .thenReturn(technicalReview1UploadedFiles);
    var summaryFileViews = List.of(
        SummaryFileView.from(
            technicalReview1UploadedFiles.get(0),
            ReverseRouter.route(on(TechnicalReviewResponseFileController.class)
                .download(application.getId(), technicalReview1.getId(), technicalReview1UploadedFiles.get(0).getId(), null))
        )
    );

    var technicalReviewSummaryItem1 =
        SummaryItem.withCard("Technical review 4",
            getTechnicalReviewDetailsSummaryCard(technicalReview4Open)
        );

    var technicalReviewSummaryItem2 =
        SummaryItem.withCard("Technical review 3",
            getTechnicalReviewDetailsSummaryCard(technicalReview3)
        );

    var technicalReviewSummaryItem3 =
        SummaryItem.withCard("Technical review 2",
            getTechnicalReviewDetailsSummaryCard(technicalReview2)
        );

    var technicalReviewSummaryItem4 =
        SummaryItem.withCards("Technical review 1",
            List.of(
                getTechnicalReviewDetailsSummaryCard(technicalReview1),
                SummaryCard.filesSummaryCardWithHeading("Response documents", summaryFileViews)
            )
        );

    assertThat(technicalReviewSummaryService.getTechnicalReviewSummaryItems(application))
        .containsExactly(technicalReviewSummaryItem1, technicalReviewSummaryItem2, technicalReviewSummaryItem3,
            technicalReviewSummaryItem4);
  }

  private SummaryCard getTechnicalReviewDetailsSummaryCard(TechnicalReview technicalReview) {

    var summaryData = SummaryDataView
        .newWithKeyValue("Review status", technicalReview.getTechnicalReviewStatus().getDisplayName())
        .addKeyValue("Request application version", technicalReview.getRequestApplicationVersion().getVersion().toString())
        .addKeyValue("Requested by", CASE_OFFICER_EPU.displayName())
        .addKeyValue("Requested on", DateUtils.format(technicalReview.getRequestedDateTime(), DATE_TIME))
        .addKeyValue("Notes for the reviewer", technicalReview.getRequestText())
        .addKeyValue("Deadline", DateUtils.format(technicalReview.getDeadlineDateTime(), DATE_TIME))
        .addKeyValue("Technical reviewer", TECHNICAL_REVIEWER_EPU_1.displayName());

    if (TechnicalReviewStatus.CLOSED.equals(technicalReview.getTechnicalReviewStatus())) {
      summaryData
          .addKeyValue("Response application version", technicalReview.getResponseApplicationVersion().getVersion().toString())
          .addKeyValue("Responded by", TECHNICAL_REVIEWER_EPU_1.displayName())
          .addKeyValue("Responded on", DateUtils.format(technicalReview.getRespondedDateTime(), DATE_TIME))
          .addKeyValue("Decision", Objects.nonNull(technicalReview.getResponseType()) // null check to cope with migrated data
              ? technicalReview.getResponseType().getDisplayName() : null)
          .addKeyValue(Objects.nonNull(technicalReview.getResponseType()) // null check to cope with migrated data
                  ? technicalReview.getResponseType().getResponseTextLabel()
                  : "Response notes",
              technicalReview.getResponseText());
    }

    return SummaryCard.simpleSummaryCard(summaryData);
  }
}
