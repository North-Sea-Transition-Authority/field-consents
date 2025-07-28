package uk.co.nstauthority.fieldconsents.teams.management.form;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
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
    form.setEmailAddress("foo");
    user.setIsAccountShared(false);
    user.setCanLogin(true);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(Optional.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isTrue();
    assertThat(errors.hasErrors()).isFalse();
  }

  @Test
  void isValid_noUsername() {
    form.setEmailAddress(null);

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }

  @Test
  void isValid_noEpaUser() {
    form.setEmailAddress("foo");

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(Optional.empty());
    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }


  @Test
  void isValid_canNotLogin() {
    form.setEmailAddress("foo");
    user.setIsAccountShared(false);
    user.setCanLogin(false);

    when(energyPortalUserService.getEnergyPortalUsersThatCanLogin("foo")).thenReturn(Optional.of(user));

    assertThat(addMemberFormValidator.isValid(form, errors)).isFalse();
    assertThat(errors.hasErrors()).isTrue();
  }
}
