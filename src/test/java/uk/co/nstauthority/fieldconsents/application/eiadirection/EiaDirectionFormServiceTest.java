package uk.co.nstauthority.fieldconsents.application.eiadirection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_3;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication3Json;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.fds.searchselector.RestSearchItem;
import uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationService;

@ExtendWith(MockitoExtension.class)
class EiaDirectionFormServiceTest {

  @Mock
  private PetsApplicationService petsApplicationService;

  @InjectMocks
  private EiaDirectionFormService eiaDirectionFormService;

  @Test
  void getPrefilledEiaDirectionRef_satIdNull() {
    assertThat(eiaDirectionFormService.getPrefilledEiaDirectionRef(null))
        .isEqualTo(EiaDirectionFormService.EMPTY_PREFILLED_ITEM);
  }

  @Test
  void getPrefilledEiaDirectionRef_satIdNotNullPetsAppNotFound() {
    when(petsApplicationService.findPetsApplicationById(SAT_ID_1, EiaDirectionFormService.EIA_DIRECTION_SEARCH_PURPOSE))
        .thenReturn(Optional.empty());

    assertThat(eiaDirectionFormService.getPrefilledEiaDirectionRef(SAT_ID_1))
        .isEqualTo(EiaDirectionFormService.EMPTY_PREFILLED_ITEM);
  }

  @Test
  void getPrefilledEiaDirectionRef_satIdNotNullPetsAppFound() {
    when(petsApplicationService.findPetsApplicationById(SAT_ID_3, EiaDirectionFormService.EIA_DIRECTION_SEARCH_PURPOSE))
        .thenReturn(Optional.of(petsApplication3Json));

    assertThat(eiaDirectionFormService.getPrefilledEiaDirectionRef(SAT_ID_3))
        .isEqualTo(new RestSearchItem(petsApplication3Json.getSelectionId(), petsApplication3Json.getSelectionText()));
  }
}