package uk.co.nstauthority.fieldconsents.energyportal.organisationgroup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.organisation.OrganisationApi;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.OrganisationGroupsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.OrganisationGroup;
import uk.co.nstauthority.fieldconsents.branding.ServiceConfigurationProperties;
import uk.co.nstauthority.fieldconsents.energyportal.api.EnergyPortalApiWrapper;

@ExtendWith(MockitoExtension.class)
class OrganisationGroupQueryServiceTest {

  private static final ServiceConfigurationProperties serviceConfigurationProperties = new ServiceConfigurationProperties(
      "name",
      "mnemonic"
  );

  @Mock
  private OrganisationApi organisationApi;

  private OrganisationGroupQueryService organisationGroupQueryService;


  private List<OrganisationGroup> groupList;

  @BeforeEach
  void setup() {
    organisationGroupQueryService = new OrganisationGroupQueryService(
        organisationApi,
        new EnergyPortalApiWrapper(serviceConfigurationProperties)
    );

    groupList = List.of(
        new OrganisationGroup(1, "Company 1", null, null, null, Collections.emptyList()),
        new OrganisationGroup(2, "Company 2", null, null, null, Collections.emptyList())
    );
  }

  @Test
  void getOrganisationGroupsByName() {
    var searchTerm = "company";

    when(organisationApi.searchOrganisationGroups(
        eq(searchTerm),
        any(OrganisationGroupsProjectionRoot.class),
        any(RequestPurpose.class)
    )).thenReturn(groupList);

    var organisationGroups =
        organisationGroupQueryService.getOrganisationGroupsByName(searchTerm);

    var argumentCaptor = ArgumentCaptor.forClass(OrganisationGroupsProjectionRoot.class);

    verify(organisationApi).searchOrganisationGroups(
        eq(searchTerm),
        argumentCaptor.capture(),
        any(RequestPurpose.class)
    );

    assertThat(argumentCaptor.getValue().getFields()).containsKeys("organisationGroupId", "name");
    assertThat(organisationGroups)
        .extracting(
            OrganisationGroupDto::getOrganisationGroupId,
            OrganisationGroupDto::getOrganisationGroupName
        )
        .containsExactly(
            tuple(
                groupList.get(0).getOrganisationGroupId(),
                groupList.get(0).getName()
            ),
            tuple(
                groupList.get(1).getOrganisationGroupId(),
                groupList.get(1).getName()
            )
        );
  }

  @Test
  void getOrganisationGroupById_verifyCallsApiWithCorrectParameters() {
    var argumentCaptor = ArgumentCaptor
        .forClass(OrganisationGroupProjectionRoot.class);
    var organisationGroup = new OrganisationGroup(
        1,
        "Royal Dutch Shell",
        "Shell",
        "shell.com",
        "ACTIVE",
        Collections.emptyList());

    when(organisationApi.findOrganisationGroup(
        eq(organisationGroup.getOrganisationGroupId()),
        any(OrganisationGroupProjectionRoot.class),
        any(RequestPurpose.class)))
        .thenReturn(Optional.of(organisationGroup));

    var returnedOrganisation = organisationGroupQueryService.getOrganisationGroupById(1);

    verify(organisationApi).findOrganisationGroup(
        eq(organisationGroup.getOrganisationGroupId()),
        argumentCaptor.capture(),
        any(RequestPurpose.class));

    assertThat(argumentCaptor.getValue().getFields())
        .containsOnly(
            entry("organisationGroupId", null),
            entry("name", null));
  }

  @Test
  void getOrganisationGroupsByIds_whenOne_verifyApiCallsAndReturn() {
    var organisationGroup = new OrganisationGroup(
        1,
        "Royal Dutch Shell",
        "Shell",
        "shell.com",
        "ACTIVE",
        Collections.emptyList());

    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of(organisationGroup.getOrganisationGroupId())),
        any(OrganisationGroupsProjectionRoot.class),
        any(RequestPurpose.class)))
        .thenReturn(List.of(organisationGroup));

    var returnedOrganisations = organisationGroupQueryService
        .getOrganisationGroupsByIds(List.of(1));

    verify(organisationApi).getAllOrganisationGroupsByIds(
        eq(List.of(organisationGroup.getOrganisationGroupId())),
        any(),
        any(RequestPurpose.class));

    assertThat(returnedOrganisations)
        .isEqualTo(List.of(organisationGroup));
  }

  @Test
  void getOrganisationGroupsByIds_whenTwo_verifyApiCallsAndReturn() {
    var organisationGroup = new OrganisationGroup(
        1,
        "Royal Dutch Shell",
        "Shell",
        "shell.com",
        "ACTIVE",
        Collections.emptyList());

    var organisationGroup2 = new OrganisationGroup(
        2,
        "Royal Dutch Shell",
        "Shell",
        "shell.com",
        "ACTIVE",
        Collections.emptyList());

    when(organisationApi.getAllOrganisationGroupsByIds(
        eq(List.of(organisationGroup.getOrganisationGroupId(), organisationGroup2.getOrganisationGroupId())),
        any(OrganisationGroupsProjectionRoot.class),
        any(RequestPurpose.class)))
        .thenReturn(List.of(organisationGroup, organisationGroup2));

    var returnedOrganisations = organisationGroupQueryService
        .getOrganisationGroupsByIds(List.of(1, 2));

    verify(organisationApi).getAllOrganisationGroupsByIds(
        eq(List.of(organisationGroup.getOrganisationGroupId(),
            organisationGroup2.getOrganisationGroupId())),
        any(),
        any(RequestPurpose.class));

    assertThat(returnedOrganisations)
        .isEqualTo(List.of(organisationGroup, organisationGroup2));
  }
}
