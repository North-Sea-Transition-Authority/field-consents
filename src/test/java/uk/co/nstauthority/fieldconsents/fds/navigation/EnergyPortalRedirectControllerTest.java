package uk.co.nstauthority.fieldconsents.fds.navigation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.energyportal.EnergyPortalConfiguration;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = EnergyPortalRedirectController.class)
class EnergyPortalRedirectControllerTest extends AbstractControllerTest {

  @Autowired
  private ApplicationContext applicationContext;

  @SecurityTest
  void redirectToEnergyPortal_noUser() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(EnergyPortalRedirectController.class)
            .redirectToEnergyPortal())))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void redirectToEnergyPortal() throws Exception {
    var workbasketUrl = applicationContext.getBean(EnergyPortalConfiguration.class).workbasketUrl();

    mockMvc.perform(get(ReverseRouter.route(on(EnergyPortalRedirectController.class)
            .redirectToEnergyPortal()))
            .with(user(user))
        )
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl(workbasketUrl)
        );
  }
}
