package uk.co.nstauthority.fieldconsents.petsapplications;

import java.util.List;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.client.RequestPurpose;
import uk.co.fivium.energyportalapi.client.pets.PetsApplicationApi;
import uk.co.fivium.energyportalapi.generated.client.PetsApplicationProjectionRoot;
import uk.co.fivium.energyportalapi.generated.client.PetsApplicationsProjectionRoot;
import uk.co.fivium.energyportalapi.generated.types.SatType;

@Service
public class PetsApplicationService {

  private final PetsApplicationApi petsApplicationApi;

  static final PetsApplicationsProjectionRoot petsApplicationsProjectionRoot =
      new PetsApplicationsProjectionRoot()
          .satId()
          .satRef()
          .satType().root()
          .status().root()
          .decision().root();

  static final PetsApplicationProjectionRoot petsApplicationProjectionRoot =
      new PetsApplicationProjectionRoot()
          .satId()
          .satRef()
          .satType().root()
          .status().root()
          .decision().root();

  @Autowired
  public PetsApplicationService(PetsApplicationApi petsApplicationApi) {
    this.petsApplicationApi = petsApplicationApi;
  }

  public List<PetsApplicationJson> searchEiaDirections(String searchTerm, String purpose) {
    return petsApplicationApi.searchPetsApplications(searchTerm,
            List.of(SatType.EIA_DIRECTION, SatType.EIA_DIRECTION_2020),
            null,
            null,
            petsApplicationsProjectionRoot,
            new RequestPurpose(purpose))
        .stream()
        .map(PetsApplicationJson::from)
        .toList();
  }

  public Optional<PetsApplicationJson> findPetsApplicationById(Integer satId, String purpose) {
    return petsApplicationApi.findPetsApplicationById(satId, petsApplicationProjectionRoot, new RequestPurpose(purpose))
        .map(PetsApplicationJson::from);
  }

  public PetsApplicationJson getPetsApplicationById(Integer satId, String purpose) {
    return findPetsApplicationById(satId, purpose)
        .orElseThrow(() -> new EntityNotFoundException("Pets application not found for satId %s".formatted(satId)));
  }

  public PetsApplicationJson getPetsApplicationByIdOrFallback(Integer satId, String purpose,
                                                              String cachedSatRef) {
    return findPetsApplicationById(satId, purpose)
        .orElseGet(() -> PetsApplicationJson.fromCachedInformation(satId, cachedSatRef));
  }
}
