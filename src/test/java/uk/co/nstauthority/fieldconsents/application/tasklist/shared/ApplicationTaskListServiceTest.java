package uk.co.nstauthority.fieldconsents.application.tasklist.shared;

import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_DETAILS_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.CONSENT_DETAILS_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_VENT_INFORMATION_DISPLAY_ORDER;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.FLARE_INFORMATION_SECTION;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.assertTaskListSection;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getConsentDetailsTaskListSection;
import static uk.co.nstauthority.fieldconsents.tasklist.TaskListTestUtil.getFlareInformationTaskListSection;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.tasklist.flare.FlareInformationTaskListSectionService;
import uk.co.nstauthority.fieldconsents.tasklist.TaskListSection;

@ExtendWith(MockitoExtension.class)
class ApplicationTaskListServiceTest {

  @Mock
  ConsentDetailsTaskListSectionService consentDetailsTaskListSectionService;

  @Mock
  FlareInformationTaskListSectionService flareInformationTaskListSectionService;

  private ApplicationVersion applicationVersion;

  private ApplicationTaskListService applicationTaskListService;


  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);
    applicationTaskListService = new ApplicationTaskListService(
        Arrays.asList(
            consentDetailsTaskListSectionService,
            flareInformationTaskListSectionService)
    );
  }

  @Test
  void getAllSections_FlareApplication() {
    when(consentDetailsTaskListSectionService.getSection(applicationVersion))
        .thenReturn(Optional.of(getConsentDetailsTaskListSection(null)));
    when(flareInformationTaskListSectionService.getSection(applicationVersion))
        .thenReturn(Optional.of(getFlareInformationTaskListSection(null)));

    List<TaskListSection> taskListSections = applicationTaskListService.getAllSections(applicationVersion);

    assertTaskListSection(taskListSections.get(0), CONSENT_DETAILS_SECTION, CONSENT_DETAILS_DISPLAY_ORDER);
    assertTaskListSection(taskListSections.get(1), FLARE_INFORMATION_SECTION, FLARE_VENT_INFORMATION_DISPLAY_ORDER);
  }
}