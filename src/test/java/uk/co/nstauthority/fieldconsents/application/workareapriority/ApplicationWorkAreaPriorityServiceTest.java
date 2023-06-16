package uk.co.nstauthority.fieldconsents.application.workareapriority;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class ApplicationWorkAreaPriorityServiceTest {

  private static final Instant CURRENT_INSTANT = Instant.now();

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Mock
  private ApplicationWorkAreaPriorityRepository applicationWorkAreaPriorityRepository;

  @Mock
  private Clock clock;

  @InjectMocks
  private ApplicationWorkAreaPriorityService applicationWorkAreaPriorityService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @ParameterizedTest
  @EnumSource(ApplicationWorkAreaPriorityGroup.class)
  void setApplicationWorkAreaPriority_whenNoExistingPriority_thenNewAndSave(
      ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup) {
    var expectedApplicationWorkAreaPriority = new ApplicationWorkAreaPriority(
        applicationVersion,
        applicationWorkAreaPriorityGroup,
        ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP,
        CURRENT_INSTANT,
        USER.wuaId()
    );

    when(applicationWorkAreaPriorityRepository
        .findByApplicationVersionAndWorkAreaPriorityGroup(applicationVersion, applicationWorkAreaPriorityGroup))
        .thenReturn(Optional.empty());
    when(clock.instant()).thenReturn(CURRENT_INSTANT);

    ArgumentCaptor<ApplicationWorkAreaPriority> applicationWorkAreaPriorityArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationWorkAreaPriority.class);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        USER,
        ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP,
        applicationWorkAreaPriorityGroup
    );

    verify(applicationWorkAreaPriorityRepository, times(1))
        .save(applicationWorkAreaPriorityArgumentCaptor.capture());

    assertThat(applicationWorkAreaPriorityArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(expectedApplicationWorkAreaPriority);
  }

  @ParameterizedTest
  @EnumSource(ApplicationWorkAreaPriorityGroup.class)
  void setApplicationWorkAreaPriority_whenExistingPriority_thenUpdateAndSave(
      ApplicationWorkAreaPriorityGroup applicationWorkAreaPriorityGroup) {
    var instantOneMinuteAhead = CURRENT_INSTANT.plusSeconds(60);
    var startingApplicationWorkAreaPriority = new ApplicationWorkAreaPriority(
        applicationVersion,
        applicationWorkAreaPriorityGroup,
        ApplicationWorkAreaPriorityReason.CASE_OFFICER_TAKE_OWNERSHIP,
        CURRENT_INSTANT,
        USER.wuaId()
    );

    var expectedApplicationWorkAreaPriority = new ApplicationWorkAreaPriority(
        applicationVersion,
        applicationWorkAreaPriorityGroup,
        ApplicationWorkAreaPriorityReason.CASE_OFFICER_RELEASE_OWNERSHIP,
        instantOneMinuteAhead,
        USER.wuaId()
    );

    when(applicationWorkAreaPriorityRepository
        .findByApplicationVersionAndWorkAreaPriorityGroup(applicationVersion, applicationWorkAreaPriorityGroup))
        .thenReturn(Optional.of(startingApplicationWorkAreaPriority));
    when(clock.instant()).thenReturn(instantOneMinuteAhead);

    ArgumentCaptor<ApplicationWorkAreaPriority> applicationWorkAreaPriorityArgumentCaptor =
        ArgumentCaptor.forClass(ApplicationWorkAreaPriority.class);

    applicationWorkAreaPriorityService.prioritiseApplicationInWorkArea(
        applicationVersion,
        USER,
        ApplicationWorkAreaPriorityReason.CASE_OFFICER_RELEASE_OWNERSHIP,
        applicationWorkAreaPriorityGroup
    );

    verify(applicationWorkAreaPriorityRepository, times(1))
        .save(applicationWorkAreaPriorityArgumentCaptor.capture());

    assertThat(applicationWorkAreaPriorityArgumentCaptor.getValue())
        .usingRecursiveComparison()
        .isEqualTo(expectedApplicationWorkAreaPriority);
  }
}
