package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;

@Service
public class ConsentFieldEquityPartnerService {

  private final ApplicationAssetService applicationAssetService;
  private final FieldEquityPartnerService fieldEquityPartnerService;
  private final ConsentFieldEquityPartnerRepository consentFieldEquityPartnerRepository;

  public ConsentFieldEquityPartnerService(ApplicationAssetService applicationAssetService,
                                          FieldEquityPartnerService fieldEquityPartnerService,
                                          ConsentFieldEquityPartnerRepository consentFieldEquityPartnerRepository) {
    this.applicationAssetService = applicationAssetService;
    this.fieldEquityPartnerService = fieldEquityPartnerService;
    this.consentFieldEquityPartnerRepository = consentFieldEquityPartnerRepository;
  }

  @Transactional
  public void saveFieldEquityPartners(Consent consent, ApplicationVersion applicationVersion) {
    if (applicationAssetService.getPrimaryAsset(applicationVersion).isField()) {
      List<Field> fields = fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion);
      if (fields.isEmpty()) {
        throw new IllegalStateException("No fields with field equity partners found for application version [%s]"
            .formatted(applicationVersion.getId()));
      }

      var distinctOrganisationUnits = fields
          .stream()
          .flatMap(field -> field.getFieldEquityPartners().stream())
          .map(uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner::getOrganisationUnit)
          .collect(Collectors.toMap(uk.co.fivium.energyportalapi.generated.types.OrganisationUnit::getOrganisationUnitId,
              organisationUnit -> organisationUnit,
              (organisationUnitId, organisationUnit) -> organisationUnitId)) // Deduplicate based on organisation unit ID
          .values();

      var fieldEquityPartners = distinctOrganisationUnits
          .stream()
          .map(organisationUnit ->
              new ConsentFieldEquityPartner(
                  consent,
                  organisationUnit.getOrganisationUnitId(),
                  organisationUnit.getName(),
                  organisationUnit.getRegisteredNumber()
              )
          )
          .toList();

      consentFieldEquityPartnerRepository.saveAll(fieldEquityPartners);
    }
  }
}
