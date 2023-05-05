package uk.co.nstauthority.fieldconsents.authorisation;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@ContextConfiguration(classes = HasPermissionInterceptorTest.TestController.class)
class HasPermissionInterceptorTest extends AbstractControllerTest {

  private static final ServiceUserDetail USER = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void preHandle_whenMethodHasNoSupportedAnnotations_thenOkResponse() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .noSupportedAnnotations()
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenUserHasRequiredPermission_thenOk() throws Exception {
    when(permissionService.hasPermission(USER, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(true);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withPermissionRequired()
        ))
            .with(user(USER)))
        .andExpect(status().isOk());
  }

  @Test
  void preHandle_whenUserDoesNotHaveRequiredPermission_thenForbidden() throws Exception {
    when(permissionService.hasPermission(USER, Set.of(RolePermission.MANAGE_INDUSTRY_TEAMS)))
        .thenReturn(false);

    mockMvc.perform(get(ReverseRouter.route(on(TestController.class)
            .withPermissionRequired()
        ))
            .with(user(USER)))
        .andExpect(status().isForbidden())
        .andExpect(status().reason(
            "User with ID %s doesn't have any of the required permissions %s"
                .formatted(USER.wuaId(), List.of(RolePermission.MANAGE_INDUSTRY_TEAMS.name()))
        ));
  }

  @Controller
  static class TestController {

    private static final String VIEW_NAME = "test-view";

    @GetMapping("/no-supported-annotation")
    ModelAndView noSupportedAnnotations() {
      return new ModelAndView(VIEW_NAME);
    }

    @GetMapping("/with-manage-industry-teams-annotation")
    @HasPermission(permissions = RolePermission.MANAGE_INDUSTRY_TEAMS)
    ModelAndView withPermissionRequired() {
      return new ModelAndView(VIEW_NAME);
    }
  }
}
