package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_GROUP_ID;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_GROUP_NAME;
import static uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormTestUtil.ORGANISATION_GROUP_REST_SEARCH_ITEM;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupQueryService;
import uk.co.nstauthority.fieldconsents.energyportal.organisationgroup.OrganisationGroupTestUtil;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;

@ExtendWith(MockitoExtension.class)
class SearchFilterFormServiceTest {

  @Mock
  private OrganisationGroupQueryService organisationGroupQueryService;

  private SearchFilterFormService searchFilterFormService;

  @BeforeEach
  void setUp() {
    searchFilterFormService = new SearchFilterFormService(organisationGroupQueryService);
  }

  @Test
  void getPrefilledOrganisationGroup_whenNullOrganisationGroup_thenEmptyRestSearchItem() {
    assertThat(searchFilterFormService.getPrefilledOrganisationGroup(null))
        .isEqualTo(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisationGroup_whenNoOrganisationGroupFound_thenEmptyRestSearchItem() {
    when(organisationGroupQueryService.getOrganisationGroupById(ORGANISATION_GROUP_ID))
        .thenReturn(Optional.empty());

    assertThat(searchFilterFormService.getPrefilledOrganisationGroup(ORGANISATION_GROUP_ID))
        .isEqualTo(RestSearchItem.EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisationGroup_whenOrganisationGroupFound() {
    var orgGroup = OrganisationGroupTestUtil.createOrganisationGroupDto(ORGANISATION_GROUP_ID, ORGANISATION_GROUP_NAME);
    when(organisationGroupQueryService.getOrganisationGroupById(ORGANISATION_GROUP_ID))
        .thenReturn(Optional.of(orgGroup));

    assertThat(searchFilterFormService.getPrefilledOrganisationGroup(ORGANISATION_GROUP_ID))
        .isEqualTo(ORGANISATION_GROUP_REST_SEARCH_ITEM);
  }
}