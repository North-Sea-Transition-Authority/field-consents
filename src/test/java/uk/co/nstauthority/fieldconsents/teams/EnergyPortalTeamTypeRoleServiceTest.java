package uk.co.nstauthority.fieldconsents.teams;

import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportal.serviceproviders.epmq.messages.ServiceProviderTeamTypeRoleDto;
import uk.co.fivium.energyportal.starter.serviceproviders.EnergyPortalServiceProviderTeamTypeRoleService;

@ExtendWith(MockitoExtension.class)
class EnergyPortalTeamTypeRoleServiceTest {

  @Mock
  private EnergyPortalServiceProviderTeamTypeRoleService serviceProviderTeamTypeRoleService;

  @InjectMocks
  private EnergyPortalTeamTypeRoleService energyPortalTeamTypeRoleService;

  private static final List<Role> ORDERED_ROLES = Arrays.stream(Role.values()).toList();

  @Test
  void publishRolesForTeamTypeMessage() {

    var regulatorServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.ACCESS_MANAGER, true),
        createServiceRoleDto(Role.INDUSTRY_ACCESS_MANAGER, false),
        createServiceRoleDto(Role.DOCUMENT_TEMPLATE_MANAGER, false),
        createServiceRoleDto(Role.CASE_OFFICER, false),
        createServiceRoleDto(Role.CASE_MANAGER, false),
        createServiceRoleDto(Role.CONSENTS_AND_AUTHORISATIONS_MANAGER, false),
        createServiceRoleDto(Role.TECHNICAL_REVIEWER, false),
        createServiceRoleDto(Role.VIEWER, false)
    );

    var consulteeServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.ACCESS_MANAGER, true),
        createServiceRoleDto(Role.ALLOCATOR, false),
        createServiceRoleDto(Role.RESPONDER, false),
        createServiceRoleDto(Role.VIEWER, false)
    );
    var organisationServiceRoleDtos = Set.of(
        createServiceRoleDto(Role.ACCESS_MANAGER, true),
        createServiceRoleDto(Role.VIEWER, false),
        createServiceRoleDto(Role.EDITOR, false),
        createServiceRoleDto(Role.SUBMITTER, false),
        createServiceRoleDto(Role.CREATOR, false),
        createServiceRoleDto(Role.FINANCE_ADMINISTRATOR, false),
        createServiceRoleDto(Role.CONSENT_RECIPIENT, false)
    );

    energyPortalTeamTypeRoleService.publishRolesForTeamTypeMessage();

    verify(serviceProviderTeamTypeRoleService).publishRolesForTeamType(regulatorServiceRoleDtos, TeamType.REGULATOR.name());
    verify(serviceProviderTeamTypeRoleService).publishRolesForTeamType(consulteeServiceRoleDtos, TeamType.CONSULTEE.name());
    verify(serviceProviderTeamTypeRoleService).publishRolesForTeamType(organisationServiceRoleDtos, TeamType.INDUSTRY.name());
  }

  private ServiceProviderTeamTypeRoleDto createServiceRoleDto(Role role, boolean isAssessManager) {
    return new ServiceProviderTeamTypeRoleDto(
        role.name(),
        role.getDisplayName(),
        role.getDescription(),
        isAssessManager,
        ORDERED_ROLES.indexOf(role)
    );
  }
}