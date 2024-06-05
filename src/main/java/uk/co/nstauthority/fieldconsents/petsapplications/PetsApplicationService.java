package uk.co.nstauthority.fieldconsents.petsapplications;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.pets.PetsApplicationApi;
import uk.co.fivium.energyportalapi.generated.client.PetsApplicationProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.PetsApplicationsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.SatType;

@Service
public class PetsApplicationService {

  private static final List<SatType> EIA_DIRECTION_SAT_TYPES = List.of(SatType.EIA_DIRECTION, SatType.EIA_DIRECTION_2020);

  static final PetsApplicationsProjectionRoot petsApplicationsProjectionRoot =
      new PetsApplicationsProjectionRoot()
          .satId()
          .satRef()
          .satType().root()
          .status().root()
          .decision().root()
          .isLatestApprovedVariation();

  static final PetsApplicationProjectionRoot petsApplicationProjectionRoot =
      new PetsApplicationProjectionRoot()
          .satId()
          .satRef()
          .satType().root()
          .status().root()
          .decision().root()
          .isLatestApprovedVariation();

  private final PetsApplicationApi petsApplicationApi;

  PetsApplicationService(PetsApplicationApi petsApplicationApi) {
    this.petsApplicationApi = petsApplicationApi;
  }

  public List<PetsApplicationJson> searchEiaDirections(String searchTerm, String purpose) {
    return petsApplicationApi.searchPetsApplications(searchTerm,
            EIA_DIRECTION_SAT_TYPES,
            null,
            null,
            petsApplicationsProjectionRoot,
            new RequestPurpose(purpose))
        .stream()
        .filter(petsApplication -> Boolean.TRUE.equals(petsApplication.getIsLatestApprovedVariation()))
        .map(PetsApplicationJson::from)
        .toList();
  }

  public Optional<PetsApplicationJson> findEiaDirectionById(Integer satId, String purpose) {
    return petsApplicationApi.findPetsApplicationById(satId, petsApplicationProjectionRoot, new RequestPurpose(purpose))
        .filter(petsApplication -> EIA_DIRECTION_SAT_TYPES.contains(petsApplication.getSatType()))
        .filter(petsApplication -> Boolean.TRUE.equals(petsApplication.getIsLatestApprovedVariation()))
        .map(PetsApplicationJson::from);
  }

  public PetsApplicationJson getEiaDirectionById(Integer satId, String purpose) {
    return findEiaDirectionById(satId, purpose)
        .orElseThrow(() -> new EntityNotFoundException("EIA direction pets application not found for satId %s".formatted(satId)));
  }

  public PetsApplicationJson getEiaDirectionByIdOrFallback(Integer satId, String purpose, String cachedSatRef) {
    return findEiaDirectionById(satId, purpose)
        .orElseGet(() -> PetsApplicationJson.fromCachedInformation(satId, cachedSatRef));
  }
}
