package uk.co.nstauthority.fieldconsents.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.search.SearchFilterFormService.ORGANISATION_UNIT_LOOKUP_PURPOSE;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;

@ExtendWith(MockitoExtension.class)
class ApplicationDataFilterFormServiceTest {

  @Mock
  private OrganisationUnitService organisationUnitService;

  @InjectMocks
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @Test
  void getPrefilledOrganisation_withEmptyOperator() {
    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(null, ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withOperator() {
    var searchRestItem = new RestSearchItem(orgUnit1Json.getSelectionId(), orgUnit1Json.getSelectionText());

    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.of(orgUnit1Json));

    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(searchRestItem);
  }

  @Test
  void getPrefilledOrganisation_withOperatorNotFound() {
    when(organisationUnitService.findOrganisationUnitById(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }
}
