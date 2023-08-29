package uk.co.nstauthority.fieldconsents.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem.EMPTY_REST_SEARCH_ITEM;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1;
import static uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitTestUtil.orgUnit1Json;
import static uk.co.nstauthority.fieldconsents.search.SearchFilterFormService.ORGANISATION_UNIT_LOOKUP_PURPOSE;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.query.ApplicationDataFilterFormService;

@ExtendWith(MockitoExtension.class)
class SearchFilterFormServiceTest {
  @Mock
  private ApplicationDataFilterFormService applicationDataFilterFormService;

  @InjectMocks
  private SearchFilterFormService searchFilterFormService;

  @Test
  void getPrefilledOrganisation_withEmptyOperator() {
    when(applicationDataFilterFormService.getPrefilledOrganisation(null, ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(EMPTY_REST_SEARCH_ITEM);

    assertThat(searchFilterFormService.getPrefilledOrganisation(null)).isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }

  @Test
  void getPrefilledOrganisation_withOperator() {
    var searchRestItem = new RestSearchItem(orgUnit1Json.getSelectionId(), orgUnit1Json.getSelectionText());
    when(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(searchRestItem);

    assertThat(searchFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId()))
        .isEqualTo(searchRestItem);
  }

  @Test
  void getPrefilledOrganisation_withOperatorNotFound() {
    when(applicationDataFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId(), ORGANISATION_UNIT_LOOKUP_PURPOSE))
        .thenReturn(EMPTY_REST_SEARCH_ITEM);

    assertThat(searchFilterFormService.getPrefilledOrganisation(orgUnit1.getOrganisationUnitId()))
        .isEqualTo(EMPTY_REST_SEARCH_ITEM);
  }
}
