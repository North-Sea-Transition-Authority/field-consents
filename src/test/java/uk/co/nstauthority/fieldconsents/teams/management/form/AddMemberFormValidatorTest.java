package uk.co.nstauthority.fieldconsents.teams.management.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import uk.co.fivium.energyportalapi.generated.types.User;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserService;

@ExtendWith(MockitoExtension.class)
class AddMemberFormValidatorTest {

  @Mock
  private EnergyPortalUserService energyPortalUserService;

  @InjectMocks
  private AddMemberFormValidator addMemberFormValidator;

  private AddMemberForm form;
  private User user;
  private BeanPropertyBindingResult errors;

  @BeforeEach
  void setUp() {
    form = new AddMemberForm();
    user = new User();
    errors = new BeanPropertyBindingResult(form, "form");
  }

  @Test
  void isValid() {
    form.setUsername("foo");
    user.setIsAccountShared(false);
    user.setCanLogin(true);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(List.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_noUsername() {
    form.setUsername(null);

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_noEpaUser() {
    form.setUsername("foo");

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(List.of());
    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_tooManyEpaUsers() {
    form.setUsername("foo");

    var user1 = new User();
    user1.setIsAccountShared(false);
    user1.setCanLogin(true);

    var user2 = new User();
    user2.setIsAccountShared(false);
    user2.setCanLogin(true);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(List.of(user1, user2));
    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_sharedAccount() {
    form.setUsername("foo");
    user.setIsAccountShared(true);
    user.setCanLogin(true);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(List.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_canNotLogin() {
    form.setUsername("foo");
    user.setIsAccountShared(false);
    user.setCanLogin(false);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(List.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }
}
