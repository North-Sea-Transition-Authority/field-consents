package uk.co.nstauthority.fieldconsents.petsapplications;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;
import static uk.co.nstauthority.fieldconsents.authentication.TestUserProvider.user;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication1Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication2Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication3Json;
import static uk.co.nstauthority.fieldconsents.util.RedirectedToLoginUrlMatcher.redirectionToLoginUrl;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import uk.co.nstauthority.fieldconsents.AbstractControllerTest;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityTest;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

@ContextConfiguration(classes = PetsApplicationRestController.class)
class PetsApplicationRestControllerTest extends AbstractControllerTest {

  @MockitoBean
  PetsApplicationService petsApplicationService;

  @SecurityTest
  void getEiaDirectionSearchResults_whenNotAuthenticated_thenRedirectedToLogin() throws Exception {
    mockMvc.perform(get(ReverseRouter.route(on(PetsApplicationRestController.class)
            .getEiaDirectionSearchResults("111"))))
        .andExpect(redirectionToLoginUrl());
  }

  @Test
  void getEiaDirectionSearchResults_assertHttpOk() throws Exception {
    when(petsApplicationService.searchEiaDirections("111",
        PetsApplicationRestController.EIA_DIRECTION_SEARCH_PURPOSE))
        .thenReturn(List.of(petsApplication1Json));

    mockMvc.perform(get(ReverseRouter.route(on(PetsApplicationRestController.class)
            .getEiaDirectionSearchResults("111")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"EIA/111/1"}]}
         """));
  }

  @Test
  void getEiaDirectionSearchResults_assertHttpOk_manyPetsApps() throws Exception {
    when(petsApplicationService.searchEiaDirections("eIa",
        PetsApplicationRestController.EIA_DIRECTION_SEARCH_PURPOSE))
        .thenReturn(List.of(petsApplication1Json, petsApplication2Json, petsApplication3Json));

    mockMvc.perform(get(ReverseRouter.route(on(PetsApplicationRestController.class)
            .getEiaDirectionSearchResults("eIa")))
            .with(user(user)))
        .andExpect(status().isOk())
        .andExpect(content().json("""
           {"results":[{"id":"1","text":"EIA/111/1"}, {"id":"2","text":"EIA/222/2"}, {"id":"3","text":"EIA/333/3"}]}
         """));
  }
}
