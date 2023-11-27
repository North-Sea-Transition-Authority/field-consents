package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;
import uk.co.nstauthority.fieldconsents.energyportal.WebUserAccountId;
import uk.co.nstauthority.fieldconsents.energyportal.user.EnergyPortalUserDto;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@ExtendWith(MockitoExtension.class)
class ApplicationDataItemServiceTest {

  @Mock
  private ApplicationDataItemDtoService applicationDataItemDtoService;

  @InjectMocks
  private ApplicationDataItemService applicationDataItemService;

  private ServiceUserDetail user;

  @BeforeEach
  void setUp() {
    user = ServiceUserDetailTestUtil.Builder().build();
  }

  @Test
  void getItemsFromDtos() {
    var dto = ApplicationDataItemUtil.getApplicationDataItemDtoForAnnualProductionInProgressForField();
    var applicationDataItemDtos = List.of(dto);

    var fieldJsonById = Map.of(field1Json.getId(), field1Json);
    when(applicationDataItemDtoService.getFieldJsonMapFromApplicationDataItemDtos(applicationDataItemDtos)).thenReturn(fieldJsonById);

    var energyPortalUserByWebUserAccountId = Map.of( mock(WebUserAccountId.class), mock(EnergyPortalUserDto.class));
    when(applicationDataItemDtoService.getEnergyPortalUserDtoMapFromApplicationDataItemDtos(applicationDataItemDtos)).thenReturn(energyPortalUserByWebUserAccountId);

    var teamType = TeamType.REGULATOR;
    var orgUnit = orgUnit1Json;

    var applicationDataItem = mock(ApplicationDataItem.class);
    when(applicationDataItemDtoService.getApplicationDataItem(
        dto,
        user,
        teamType,
        Map.of(orgUnit.organisationUnitId(), orgUnit.name()),
        fieldJsonById,
        energyPortalUserByWebUserAccountId
    )).thenReturn(applicationDataItem);

    assertThat(applicationDataItemService.getItemsFromDtos(
        applicationDataItemDtos,
        List.of(orgUnit),
        teamType,
        user
    )).containsExactly(applicationDataItem);
  }

  @ParameterizedTest
  @EnumSource(TeamType.class)
  void getItemsFromDtos_emptyDtoCollection(TeamType teamType) {
    assertThat(applicationDataItemService.getItemsFromDtos(Collections.emptyList(), List.of(orgUnit1Json), teamType, user)).isEmpty();
  }

}
