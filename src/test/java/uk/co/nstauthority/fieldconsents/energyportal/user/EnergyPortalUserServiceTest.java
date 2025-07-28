package uk.co.nstauthority.fieldconsents.energyportal.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.user.UserApi;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;

@ExtendWith(MockitoExtension.class)
class EnergyPortalUserServiceTest {

  @Mock
  private UserApi userApi;

  @InjectMocks
  private EnergyPortalUserService energyPortalUserService;

  @Test
  void getEnergyPortalUsersThatCanLogin_whenNoResults_thenEmptyList() {
    var username = "username";
    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Collections.emptyList());

    assertTrue(energyPortalUserService.getEnergyPortalUsersThatCanLogin(username).isEmpty());
  }

  @Test
  void getEnergyPortalUsersThatCanLogin_whenUserFoundAndCanLogIn_thenPopulatedListCorrectlyMapped() {
    var username = "username";
    var expectedUser = EpaUserTestUtil.Builder()
        .canLogin(true)
        .build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(List.of(expectedUser));

    assertThat(energyPortalUserService.getEnergyPortalUsersThatCanLogin(username)).contains(expectedUser);
  }

  @Test
  void getEnergyPortalUsersThatCanLogin_whenUsersFound_thenOnlyThoseWithCanLoginTrueReturned() {
    var username = "username";

    var canLoginUser = EpaUserTestUtil.Builder()
        .canLogin(true)
        .withWebUserAccountId(100)
        .build();

    var notLoginUser = EpaUserTestUtil.Builder()
        .canLogin(false)
        .withWebUserAccountId(200)
        .build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(username),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(List.of(
        canLoginUser,
        notLoginUser
    ));

    assertThat(energyPortalUserService.getEnergyPortalUsersThatCanLogin(username)).contains(canLoginUser);
  }

  @Test
  void getEnergyPortalUsersThatCanLogin_whenMultipleUsersFound_thenThrow() {
    var emailAddress = "emailAddress";

    var canLoginUser = EpaUserTestUtil.Builder()
        .canLogin(true)
        .withWebUserAccountId(100)
        .build();

    var notLoginUser = EpaUserTestUtil.Builder()
        .canLogin(true)
        .withWebUserAccountId(200)
        .build();

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByEmail(
        eq(emailAddress),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(List.of(
        canLoginUser,
        notLoginUser
    ));

    assertThatThrownBy(() -> energyPortalUserService.getEnergyPortalUsersThatCanLogin(emailAddress))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("More than one UK Energy Portal user exists with the email address %s".formatted(emailAddress));
  }

  @Test
  void findByWuaIds_whenNoResults_thenEmptyList() {

    var webUserAccountId = new WebUserAccountId(123);

    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByIds(
        eq(List.of(webUserAccountId.toInt())),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Collections.emptyList());

    assertTrue(energyPortalUserService.findByWuaIds(List.of(webUserAccountId)).isEmpty());
  }

  @Test
  void findByWuaIds_whenResults_thenPopulatedListCorrectlyMapped() {
    var webUserAccountId = new WebUserAccountId(123);
    var expectedUser = EpaUserTestUtil.Builder().build();
    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

      when(userApi.searchUsersByIds(
          eq(List.of(webUserAccountId.toInt())),
          eq(userProjectionRoot),
          any(RequestPurpose.class)
      )).thenReturn(List.of(expectedUser));

    assertThat(energyPortalUserService.findByWuaIds(List.of(webUserAccountId)))
        .extracting(
            EnergyPortalUserDto::webUserAccountId,
            EnergyPortalUserDto::title,
            EnergyPortalUserDto::forename,
            EnergyPortalUserDto::surname,
            EnergyPortalUserDto::emailAddress,
            EnergyPortalUserDto::telephoneNumber,
            EnergyPortalUserDto::isSharedAccount,
            EnergyPortalUserDto::canLogin
        )
        .containsExactly(
            tuple(
                Long.valueOf(expectedUser.getWebUserAccountId()),
                expectedUser.getTitle(),
                expectedUser.getForename(),
                expectedUser.getSurname(),
                expectedUser.getPrimaryEmailAddress(),
                expectedUser.getTelephoneNumber(),
                expectedUser.getIsAccountShared(),
                expectedUser.getCanLogin()
            )
        );
  }

  @Test
  void getEnergyPortalUserMap_whenNoResults_thenEmptyMap() {
    var webUserAccountId = new WebUserAccountId(123);
    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByIds(
        eq(List.of(webUserAccountId.toInt())),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Collections.emptyList());

    assertTrue(energyPortalUserService.getEnergyPortalUserMap(List.of(webUserAccountId)).isEmpty());
  }

  @Test
  void getEnergyPortalUserMap_whenResults_thenPopulatedListCorrectlyMapped() {
    var webUserAccountId1 = new WebUserAccountId(123);
    var expectedUser1 = EpaUserTestUtil.Builder().withWebUserAccountId(webUserAccountId1.toInt()).build();
    var webUserAccountId2 = new WebUserAccountId(456);
    var expectedUser2 = EpaUserTestUtil.Builder().withWebUserAccountId(webUserAccountId2.toInt()).build();
    var userProjectionRoot = EnergyPortalUserService.USERS_PROJECT_ROOT;

    when(userApi.searchUsersByIds(
        eq(List.of(webUserAccountId1.toInt(), webUserAccountId2.toInt())),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(List.of(expectedUser1, expectedUser2));

    var expectedEnergyPortalUser1 = EnergyPortalUserDto.from(expectedUser1);
    var expectedEnergyPortalUser2 = EnergyPortalUserDto.from(expectedUser2);

    assertThat(energyPortalUserService.getEnergyPortalUserMap(List.of(webUserAccountId1, webUserAccountId2)))
        .containsOnly(
            entry(WebUserAccountId.from(expectedEnergyPortalUser1.webUserAccountId()), expectedEnergyPortalUser1),
            entry(WebUserAccountId.from(expectedEnergyPortalUser2.webUserAccountId()), expectedEnergyPortalUser2)
        );
  }

  @Test
  void findByWuaId_whenFound_thenPopulatedOptional() {
    var expectedUser = EpaUserTestUtil.Builder().build();
    var webUserAccountId = new WebUserAccountId(expectedUser.getWebUserAccountId());
    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(expectedUser));

    var resultingUser = energyPortalUserService.findByWuaId(webUserAccountId);

    assertThat(resultingUser).isPresent();
    assertThat(resultingUser.get())
        .extracting(
            EnergyPortalUserDto::webUserAccountId,
            EnergyPortalUserDto::title,
            EnergyPortalUserDto::forename,
            EnergyPortalUserDto::surname,
            EnergyPortalUserDto::emailAddress,
            EnergyPortalUserDto::telephoneNumber,
            EnergyPortalUserDto::isSharedAccount,
            EnergyPortalUserDto::canLogin
        )
        .containsExactly(
            Long.valueOf(expectedUser.getWebUserAccountId()),
            expectedUser.getTitle(),
            expectedUser.getForename(),
            expectedUser.getSurname(),
            expectedUser.getPrimaryEmailAddress(),
            expectedUser.getTelephoneNumber(),
            expectedUser.getIsAccountShared(),
            expectedUser.getCanLogin()
        );
  }

  @Test
  void findByWuaId_whenNotFound_thenEmptyOptional() {
    var webUserAccountId = new WebUserAccountId(123);
    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Optional.empty());

    assertThat(energyPortalUserService.findByWuaId(webUserAccountId)).isEmpty();
  }

  @Test
  void getByWuaId_whenFound_thenUserReturned() {
    var expectedUser = EpaUserTestUtil.Builder().build();
    var webUserAccountId = new WebUserAccountId(expectedUser.getWebUserAccountId());

    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(expectedUser));

    var resultingUser = energyPortalUserService.getByWuaId(webUserAccountId);

    assertThat(resultingUser)
        .extracting(
            EnergyPortalUserDto::webUserAccountId,
            EnergyPortalUserDto::title,
            EnergyPortalUserDto::forename,
            EnergyPortalUserDto::surname,
            EnergyPortalUserDto::emailAddress,
            EnergyPortalUserDto::telephoneNumber,
            EnergyPortalUserDto::isSharedAccount,
            EnergyPortalUserDto::canLogin
        )
        .containsExactly(
            Long.valueOf(expectedUser.getWebUserAccountId()),
            expectedUser.getTitle(),
            expectedUser.getForename(),
            expectedUser.getSurname(),
            expectedUser.getPrimaryEmailAddress(),
            expectedUser.getTelephoneNumber(),
            expectedUser.getIsAccountShared(),
            expectedUser.getCanLogin()
        );
  }

  @Test
  void getByWuaId_whenNotFound_thenThrow() {
    var webUserAccountId = new WebUserAccountId(123);
    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Optional.empty());

    assertThatThrownBy(() -> energyPortalUserService.getByWuaId(webUserAccountId))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessage("Energy portal user with wua id %s not found"
            .formatted(webUserAccountId.toString()));
  }

  @Test
  void getServiceUserByWuaId() {
    var expectedPortalUser = EpaUserTestUtil.Builder().build();
    var expectedUser = ServiceUserDetailTestUtil.Builder()
        .withWuaId(expectedPortalUser.getWebUserAccountId().longValue())
        .withForename(expectedPortalUser.getForename())
        .withSurname(expectedPortalUser.getSurname())
        .withEmailAddress(expectedPortalUser.getPrimaryEmailAddress())
        .build();
    var webUserAccountId = new WebUserAccountId(expectedPortalUser.getWebUserAccountId());
    var userProjectionRoot = EnergyPortalUserService.USER_PROJECT_ROOT;

    when(userApi.findUserById(
        eq(webUserAccountId.toInt()),
        eq(userProjectionRoot),
        any(RequestPurpose.class)
    )).thenReturn(Optional.of(expectedPortalUser));

    var resultingUser = energyPortalUserService.getServiceUserByWuaId(webUserAccountId);

    assertThat(resultingUser)
        .extracting(
            ServiceUserDetail::wuaId,
            ServiceUserDetail::forename,
            ServiceUserDetail::surname,
            ServiceUserDetail::emailAddress
        )
        .containsExactly(
            expectedUser.wuaId(),
            expectedUser.forename(),
            expectedUser.surname(),
            expectedUser.emailAddress()
        );
  }
}
