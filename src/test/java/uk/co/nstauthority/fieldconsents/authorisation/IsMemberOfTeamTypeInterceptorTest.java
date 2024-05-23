package uk.co.nstauthority.fieldconsents.authorisation;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil.APPLICATION_ID;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractApplicationControllerTest;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ContextConfiguration(classes = IsMemberOfTeamTypeInterceptorTest.TestController.class)
class IsMemberOfTeamTypeInterceptorTest extends AbstractApplicationControllerTest {

  private ApplicationVersion applicationVersionSubmitted;

  @BeforeEach
  void setUp() {
    applicationVersionSubmitted = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.FLARE);
  }

  @SecurityTest
  void noSecurityAnnotation() {
    assertThatThrownBy(() ->
        mockMvc.perform(get(ReverseRouter.route(on(TestController.class).noSecurityAnnotation()))
            .with(user(user))
        ))
        .hasMessageEndingWith("Controllers must be annotated with @Security");
  }

  @SecurityTest
  void preHandle_whenIndustryUserAndIndustryTeamType_thenOk() throws Exception {
    when(teamService.isIndustryUser(user)).thenReturn(true);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionSubmitted));

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withIndustryTeamTypeRequired(APPLICATION_ID)
        ))
        .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void preHandle_whenNonIndustryAndIndustryTeamType_thenForbidden() throws Exception {
    when(teamService.isIndustryUser(user)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withIndustryTeamTypeRequired(APPLICATION_ID)
        ))
        .with(user(user)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(
            "User [%s] is not in the team type [%s] required to access case processing"
                .formatted(user.wuaId(), TeamType.INDUSTRY)
        ));
  }

  @SecurityTest
  void preHandle_whenRegulatorUserAndRegulatorTeamType_thenOk() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(true);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionSubmitted));

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withRegulatorTeamTypeRequired(APPLICATION_ID)
        ))
        .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void preHandle_whenNonRegulatorUserAndRegulatorTeamType_thenForbidden() throws Exception {
    when(teamService.isRegulatorUser(user)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withRegulatorTeamTypeRequired(APPLICATION_ID)
        ))
        .with(user(user)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(
            "User [%s] is not in the team type [%s] required to access case processing"
                .formatted(user.wuaId(), TeamType.REGULATOR)
        ));
  }

  @SecurityTest
  void preHandle_whenConsulteeUserAndConsulteeTeamType_thenOk() throws Exception {
    when(teamService.isConsulteeUser(user)).thenReturn(true);
    when(applicationVersionService.findLatestApplicationVersion(APPLICATION_ID))
        .thenReturn(Optional.of(applicationVersionSubmitted));

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withConsulteeTeamTypeRequired(APPLICATION_ID)
        ))
        .with(user(user)))
        .andExpect(status().isOk());
  }

  @SecurityTest
  void preHandle_whenNonConsulteeUserAndConsulteeTeamType_thenForbidden() throws Exception {
    when(teamService.isConsulteeUser(user)).thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withConsulteeTeamTypeRequired(APPLICATION_ID)
        ))
        .with(user(user)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(
            "User [%s] is not in the team type [%s] required to access case processing"
                .formatted(user.wuaId(), TeamType.OPRED)
        ));
  }

  @RequestMapping("/applications")
  @Controller
  static class TestController {

    private static final String VIEW_NAME = "test-view";

    @GetMapping("/no-annotation")
    public ModelAndView noSecurityAnnotation() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("{applicationId}/can-access-industry-case-processing")
    @IsMemberOfTeamType(teamType = TeamType.INDUSTRY)
    ModelAndView withIndustryTeamTypeRequired(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("{applicationId}/can-access-regulator-case-processing")
    @IsMemberOfTeamType(teamType = TeamType.REGULATOR)
    ModelAndView withRegulatorTeamTypeRequired(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("{applicationId}/can-access-consultee-case-processing")
    @IsMemberOfTeamType(teamType = TeamType.OPRED)
    ModelAndView withConsulteeTeamTypeRequired(@PathVariable Integer applicationId) {
      return new ModelAndView(VIEW_NAME);
    }
  }
}
