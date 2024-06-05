package uk.co.nstauthority.fieldconsents.petsapplications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_ID_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.SAT_REF_1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication1;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication1Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication2Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplication3Json;
import static uk.co.nstauthority.fieldconsents.petsapplications.PetsApplicationTestUtil.petsApplications;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.client.pets.PetsApplicationApi;
import uk.co.fivium.energyportalapi.generated.types.PetsApplication;
import uk.co.fivium.energyportalapi.generated.types.SatDecision;
import uk.co.fivium.energyportalapi.generated.types.SatStatus;
import uk.co.fivium.energyportalapi.generated.types.SatType;

@ExtendWith(MockitoExtension.class)
class PetsApplicationServiceTest {

  private static final String PETS_SERVICE_PURPOSE = "Pets application service test purpose";

  @Mock
  private PetsApplicationApi petsApplicationApi;

  @InjectMocks
  @Spy
  private PetsApplicationService petsApplicationService;

  @Test
  void searchEiaDirections_onePetsApp() {
    when(petsApplicationApi.searchPetsApplications(eq("111"), any(), any(), any(), any(), any()))
        .thenReturn(List.of(petsApplication1));

    List<PetsApplicationJson> petsApplicationJsons = petsApplicationService.searchEiaDirections("111",
        PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsons).containsExactly(petsApplication1Json);
  }

  @Test
  void searchEiaDirections_manyPetsApps() {
    when(petsApplicationApi.searchPetsApplications(eq("I"), any(), any(), any(), any(), any()))
        .thenReturn(petsApplications);

    List<PetsApplicationJson> petsApplicationJsons = petsApplicationService.searchEiaDirections("I",
        PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsons).containsExactly(petsApplication1Json, petsApplication2Json, petsApplication3Json);
  }

  @ParameterizedTest
  @MethodSource("getFalseAndNullBooleanArguments")
  void searchEiaDirections_petsApplicationWithFalseOrNullIsLatestApprovedVariationNotIncluded(Boolean isLatestApprovedVariation) {
    var petsApplication = PetsApplication.newBuilder()
        .satId(SAT_ID_1)
        .satRef(SAT_REF_1)
        .satType(SatType.EIA_DIRECTION)
        .status(SatStatus.COMPLETED)
        .decision(SatDecision.APPROVE)
        .isLatestApprovedVariation(isLatestApprovedVariation)
        .build();

    when(petsApplicationApi.searchPetsApplications(eq("111"), any(), any(), any(), any(), any()))
        .thenReturn(List.of(petsApplication, petsApplication1));

    List<PetsApplicationJson> petsApplicationJsons = petsApplicationService.searchEiaDirections("111",
        PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsons).containsExactly(petsApplication1Json);
  }

  @Test
  void findEiaDirectionById_notExists() {
    when(petsApplicationApi.findPetsApplicationById(eq(0), any(), any()))
        .thenReturn(Optional.empty());

    var petsApplicationJsonOptional = petsApplicationService
        .findEiaDirectionById(0, PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsonOptional).isEmpty();
  }

  @Test
  void findEiaDirectionById_exists() {
    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication1.getSatId()), any(), any()))
        .thenReturn(Optional.of(petsApplication1));

    var petsApplicationJsonOptional = petsApplicationService
        .findEiaDirectionById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsonOptional).contains(petsApplication1Json);
  }

  @ParameterizedTest
  @EnumSource(value = SatType.class, mode = EnumSource.Mode.EXCLUDE, names = { "EIA_DIRECTION", "EIA_DIRECTION_2020" })
  void findEiaDirectionById_existsAndSatTypeIsNotEiaDirectionOrEiaDirection2020(SatType satType) {
    var petsApplication = PetsApplication.newBuilder()
        .satId(SAT_ID_1)
        .satRef(SAT_REF_1)
        .satType(satType)
        .status(SatStatus.COMPLETED)
        .decision(SatDecision.APPROVE)
        .isLatestApprovedVariation(null)
        .build();

    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication.getSatId()), any(), any()))
        .thenReturn(Optional.of(petsApplication));

    var petsApplicationJsonOptional = petsApplicationService
        .findEiaDirectionById(petsApplication.getSatId(), PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsonOptional).isEmpty();
  }

  @ParameterizedTest
  @MethodSource("getFalseAndNullBooleanArguments")
  void findEiaDirectionById_isLatestApprovedVariationFalseOrNull(Boolean isLatestApprovedVariation) {
    var petsApplication = PetsApplication.newBuilder()
        .satId(SAT_ID_1)
        .satRef(SAT_REF_1)
        .satType(SatType.EIA_DIRECTION)
        .status(SatStatus.COMPLETED)
        .decision(SatDecision.APPROVE)
        .isLatestApprovedVariation(isLatestApprovedVariation)
        .build();

    when(petsApplicationApi.findPetsApplicationById(eq(petsApplication.getSatId()), any(), any()))
        .thenReturn(Optional.of(petsApplication));

    var petsApplicationJsonOptional = petsApplicationService
        .findEiaDirectionById(petsApplication.getSatId(), PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJsonOptional).isEmpty();
  }

  private static Stream<Arguments> getFalseAndNullBooleanArguments() {
    return Stream.of(
        Arguments.of(false),
        Arguments.of((Boolean) null)
    );
  }

  @Test
  void getEiaDirectionById_exists() {
    doReturn(Optional.of(petsApplication1Json))
        .when(petsApplicationService)
        .findEiaDirectionById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);

    var petsApplicationJson = petsApplicationService
        .getEiaDirectionById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);
    assertThat(petsApplicationJson).isEqualTo(petsApplication1Json);
  }

  @Test
  void getEiaDirectionById_notExists() {
    doReturn(Optional.empty())
        .when(petsApplicationService)
        .findEiaDirectionById(SAT_ID_1, PETS_SERVICE_PURPOSE);

    assertThatThrownBy(() -> petsApplicationService
        .getEiaDirectionById(SAT_ID_1, PETS_SERVICE_PURPOSE))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("EIA direction pets application not found for satId %s".formatted(SAT_ID_1));
  }

  @Test
  void getEiaDirectionByIdOrFallback_exists() {
    doReturn(Optional.of(petsApplication1Json))
        .when(petsApplicationService)
        .findEiaDirectionById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);

    var petsApplicationJson = petsApplicationService.getEiaDirectionByIdOrFallback(
        petsApplication1.getSatId(), PETS_SERVICE_PURPOSE, petsApplication1.getSatRef());
    assertThat(petsApplicationJson).isEqualTo(petsApplication1Json);
  }

  @Test
  void getEiaDirectionByIdOrFallback_notExistsUsesFallback() {
    doReturn(Optional.empty())
        .when(petsApplicationService)
        .findEiaDirectionById(petsApplication1.getSatId(), PETS_SERVICE_PURPOSE);

    var petsApplicationJson = petsApplicationService.getEiaDirectionByIdOrFallback(
        petsApplication1.getSatId(), PETS_SERVICE_PURPOSE, petsApplication1.getSatRef());
    assertThat(petsApplicationJson)
        .isEqualTo(new PetsApplicationJson(petsApplication1.getSatId(), petsApplication1.getSatRef(), null, null, null));
  }
}
