package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FormattedFieldEquityPartner;

@Service
public class ConsentFieldEquityPartnerService {

  private final FieldEquityPartnerService fieldEquityPartnerService;
  private final ConsentFieldEquityPartnerRepository consentFieldEquityPartnerRepository;

  public ConsentFieldEquityPartnerService(FieldEquityPartnerService fieldEquityPartnerService,
                                          ConsentFieldEquityPartnerRepository consentFieldEquityPartnerRepository) {
    this.fieldEquityPartnerService = fieldEquityPartnerService;
    this.consentFieldEquityPartnerRepository = consentFieldEquityPartnerRepository;
  }

  @Transactional
  public void saveFieldEquityPartners(Consent consent, ApplicationVersion applicationVersion) {
    List<Field> fields = fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion);
    if (fields.isEmpty()) {
      throw new IllegalStateException("No fields with field equity partners found for application version [%s]"
          .formatted(applicationVersion.getId()));
    }

    var distinctOrganisationUnits = fields
        .stream()
        .flatMap(field -> field.getFieldEquityPartners().stream())
        .map(FieldEquityPartner::getOrganisationUnit)
        .collect(Collectors.toMap(
            OrganisationUnit::getOrganisationUnitId,
            organisationUnit -> organisationUnit,
            (a, b) -> a))
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

  public List<ConsentFieldEquityPartner> getConsentFieldEquityPartnersByConsent(Consent consent) {
    return consentFieldEquityPartnerRepository.findAllByConsent(consent);
  }

  public ConsentFieldEquityPartnersView getConsentFieldEquityPartnersView(Consent consent) {
    var formattedFieldEquityPartners = getConsentFieldEquityPartnersByConsent(consent)
        .stream()
        .map(consentFieldEquityPartner ->
            new FormattedFieldEquityPartner(
                consentFieldEquityPartner.getOrganisationName(),
                consentFieldEquityPartner.getRegisteredNumber()
            )
        )
        .sorted()
        .toList();

    return new ConsentFieldEquityPartnersView(formattedFieldEquityPartners);
  }
}
