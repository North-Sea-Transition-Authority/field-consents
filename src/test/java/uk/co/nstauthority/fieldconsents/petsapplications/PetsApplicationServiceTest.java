package uk.co.nstauthority.fieldconsents.petsapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication1Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication2Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication3Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplications;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.pets.PetsApplicationApi;

@ExtendWith(MockitoExtension.class)
class PetsApplicationServiceTest {

  private static final String PETS_SERVICE_PURPOSE = "Pets application service test purpose";

  PetsApplicationService petsApplicationService;

  @Mock
  PetsApplicationApi petsApplicationApi;

  @BeforeEach
  void setUp() {
    petsApplicationService = new PetsApplicationService(petsApplicationApi);
  }

  @Test
  void searchEiaDirections_manyPetsApps() {
    when(petsApplicationApi.searchPetsApplications(eq("I"), any(), any(), any(), any(), any()))
        .thenReturn(petsApplications);

    List<PetsApplicationJson> petsApplicationJsons = petsApplicationService.searchEiaDirections("I",
        PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsons).containsExactly(petsApplication1Json, petsApplication2Json, petsApplication3Json);
  }

  @Test
  void searchEiaDirections_onePetsApp() {
    when(petsApplicationApi.searchPetsApplications(eq("111"), any(), any(), any(), any(), any()))
        .thenReturn(List.of(petsApplication1));

    List<PetsApplicationJson> petsApplicationJsons = petsApplicationService.searchEiaDirections("111",
        PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsons).containsExactly(petsApplication1Json);
  }



  @Test
  void findPetsApplicationById_exists() {
    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication1.getSatId()), any(), any()))
        .thenReturn(Optional.of(petsApplication1));

    var petsApplicationJsonOptional = petsApplicationService
        .findPetsApplicationById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsonOptional).contains(petsApplication1Json);
  }

  @Test
  void findPetsApplicationById_notExists() {
    when(petsApplicationApi.findPetsApplicationById(eq(0), any(), any()))
        .thenReturn(Optional.empty());

    var petsApplicationJsonOptional = petsApplicationService
        .findPetsApplicationById(0, PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsonOptional).isEmpty();
  }

  @Test
  void getPetsApplicationById_exists() {
    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication1.getSatId()), any(), any()))
        .thenReturn(Optional.of(petsApplication1));

    var petsApplicationJson = petsApplicationService
        .getPetsApplicationById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJson).isEqualTo(petsApplication1Json);
  }

  @Test
  void getPetsApplicationById_notExists() {
    when(petsApplicationApi.findPetsApplicationById(eq(SAT_ID_1), any(), any()))
        .thenReturn(Optional.empty());

    assertThatThrownBy(() -> petsApplicationService
        .getPetsApplicationById(SAT_ID_1, PETS_SERVICE_PURPOSE))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("Pets application not found for satId %s".formatted(SAT_ID_1));
  }

  @Test
  void getPetsApplicationByIdOrFallback_exists() {
    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication1.getSatId()), any(), any()))
        .thenReturn(Optional.of(petsApplication1));

    var petsApplicationJson = petsApplicationService.getPetsApplicationByIdOrFallback(
        petsApplication1.getSatId(), PETS_SERVICE_PURPOSE, petsApplication1.getSatRef());
    assertThat(petsApplicationJson).isEqualTo(petsApplication1Json);
  }

  @Test
  void getPetsApplicationByIdOrFallback_notExistsUsesFallback() {
    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication1.getSatId()), any(), any()))
        .thenReturn(Optional.empty());

    var petsApplicationJson = petsApplicationService.getPetsApplicationByIdOrFallback(
        petsApplication1.getSatId(), PETS_SERVICE_PURPOSE, petsApplication1.getSatRef());
    assertThat(petsApplicationJson)
        .isEqualTo(new PetsApplicationJson(petsApplication1.getSatId(), petsApplication1.getSatRef(), null, null, null));
  }
}
