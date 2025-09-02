package uk.co.nstauthority.fieldconsents.teams;

import static org.mockito.Mockito.verify;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamTypeService;

@ExtendWith(MockitoExtension.class)
class EnergyPortalTeamTypesServiceTest {

  @Mock
  private EnergyPortalServiceProviderTeamTypeService serviceProviderTeamTypeService;

  @InjectMocks
  private EnergyPortalTeamTypesService energyPortalTeamTypesService;

  @Test
  void publishSetTeamTypesMessage() {
    energyPortalTeamTypesService.publishTeamTypesMessage();
    verify(serviceProviderTeamTypeService).publishTeamTypes(Set.of(
        TeamType.REGULATOR.name(),
        TeamType.CONSULTEE.name(),
        TeamType.INDUSTRY.name()
    ));
  }
}