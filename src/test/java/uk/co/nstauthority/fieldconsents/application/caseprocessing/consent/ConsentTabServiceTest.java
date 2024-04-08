package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.ModelAndView;
import uk.co.fivum.fileuploadlibrary.core.UploadedFileTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;
import uk.co.nstauthority.fieldconsents.file.FieldConsentsFileService;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.summary.SummaryFileView;

@ExtendWith(MockitoExtension.class)
class ConsentTabServiceTest {

  @Mock
  private ConsentService consentService;

  @Mock
  private FieldConsentsFileService fieldConsentsFileService;

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  @Spy
  private ConsentTabService consentTabService;

  @Test
  void addConsentTabContentToModelAndView_consentDoesNotExist() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var modelAndView = new ModelAndView();

    when(consentService.findConsent(application)).thenReturn(Optional.empty());

    consentTabService.addConsentTabContentToModelAndView(application, modelAndView);

    assertThat(modelAndView.getModel()).doesNotContainKey("consentTabConsentSummaryView");
  }

  @Test
  void addConsentTabContentToModelAndView_consentExists() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var modelAndView = new ModelAndView();

    var consent = ConsentTestUtil.newBuilder().build();

    var energyPortalUserDto = mock(EnergyPortalUserDto.class);

    var generatedConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var generatedConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    var supportingConsentDocumentSummaryFileView1 = mock(SummaryFileView.class);
    var supportingConsentDocumentSummaryFileView2 = mock(SummaryFileView.class);

    when(consentService.findConsent(application)).thenReturn(Optional.of(consent));
    when(energyPortalUserService.getByWuaId(WebUserAccountId.from(consent.getIssuedByWuaId()))).thenReturn(energyPortalUserDto);

    doReturn(Stream.of(generatedConsentDocumentSummaryFileView1, generatedConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getGeneratedConsentDocumentSummaryFileViews(application, consent);

    doReturn(Stream.of(supportingConsentDocumentSummaryFileView1, supportingConsentDocumentSummaryFileView2))
        .when(consentTabService)
        .getSupportingConsentDocumentSummaryFileViews(application, consent);

    consentTabService.addConsentTabContentToModelAndView(application, modelAndView);

    assertThat(modelAndView.getModel()).containsEntry(
        "consentTabConsentSummaryView",
        ConsentTabConsentSummaryView.from(
            consent,
            ServiceUserDetail.from(energyPortalUserDto),
            List.of(
                generatedConsentDocumentSummaryFileView1,
                generatedConsentDocumentSummaryFileView2,
                supportingConsentDocumentSummaryFileView1,
                supportingConsentDocumentSummaryFileView2
            )
        )
    );
  }

  @Test
  void getGeneratedConsentDocumentSummaryFileViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consent = ConsentTestUtil.newBuilder().build();

    var generatedConsentDocumentConsentFileUsage = ConsentFileUsage.generatedConsentDocumentFrom(consent);

    var uploadedFile1 = UploadedFileTestUtil.newBuilder().build();
    var uploadedFile2 = UploadedFileTestUtil.newBuilder().build();

    when(fieldConsentsFileService.getUploadedFiles(generatedConsentDocumentConsentFileUsage))
        .thenReturn(List.of(uploadedFile1, uploadedFile2));

    assertThat(consentTabService.getGeneratedConsentDocumentSummaryFileViews(application, consent)).containsExactly(
        SummaryFileView.from(
            uploadedFile1,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadGeneratedConsentDocument(application.getId(), uploadedFile1.getId(), null))
        ),
        SummaryFileView.from(
            uploadedFile2,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadGeneratedConsentDocument(application.getId(), uploadedFile2.getId(), null))
        )
    );
  }

  @Test
  void getSupportingConsentDocumentSummaryFileViews() {
    var application = ApplicationTestUtil.getNewApplicationWithType(ApplicationType.PRODUCTION);
    var consent = ConsentTestUtil.newBuilder().build();

    var supportingConsentDocumentConsentFileUsage = ConsentFileUsage.supportingConsentDocumentFrom(consent);

    var uploadedFile1 = UploadedFileTestUtil.newBuilder().build();
    var uploadedFile2 = UploadedFileTestUtil.newBuilder().build();

    when(fieldConsentsFileService.getUploadedFiles(supportingConsentDocumentConsentFileUsage))
        .thenReturn(List.of(uploadedFile1, uploadedFile2));

    assertThat(consentTabService.getSupportingConsentDocumentSummaryFileViews(application, consent)).containsExactly(
        SummaryFileView.from(
            uploadedFile1,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadSupportingConsentDocument(application.getId(), uploadedFile1.getId(), null))
        ),
        SummaryFileView.from(
            uploadedFile2,
            ReverseRouter.route(on(ConsentFileController.class)
                .downloadSupportingConsentDocument(application.getId(), uploadedFile2.getId(), null))
        )
    );
  }
}
