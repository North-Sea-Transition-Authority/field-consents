package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.fieldequitypartner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.fivium.energyportalapi.generated.types.Field;
import uk.co.fivium.energyportalapi.generated.types.FieldEquityPartner;
import uk.co.fivium.energyportalapi.generated.types.OrganisationUnit;
import uk.co.nstauthority.fieldconsents.application.ApplicationTestUtil;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnerService;

@ExtendWith(MockitoExtension.class)
class ConsentFieldEquityPartnerServiceTest {

  @Mock
  private FieldEquityPartnerService fieldEquityPartnerService;

  @Mock
  private ConsentFieldEquityPartnerRepository consentFieldEquityPartnerRepository;

  @Captor
  private ArgumentCaptor<List<ConsentFieldEquityPartner>> consentFieldEquityPartnersArgumentCaptor;

  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  private ApplicationVersion applicationVersion;

  private Consent consent;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);
    consent = new Consent(1);
    consentFieldEquityPartnerService = new ConsentFieldEquityPartnerService(
        fieldEquityPartnerService,
        consentFieldEquityPartnerRepository
    );
  }

  @Test
  void saveFieldEquityPartners_whenNoFieldEquityPartners_thenThrowException() {
    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion))
        .thenReturn(Collections.emptyList());

    assertThatThrownBy(() -> consentFieldEquityPartnerService.saveFieldEquityPartners(consent, applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No fields with field equity partners found for application version [1]");

    verify(consentFieldEquityPartnerRepository, never()).save(any());
  }

  @Test
  void saveFieldEquityPartners_whenApplicationIsForField_thenFieldEquityPartnersAreSaved() {
    var fields = List.of(
        getFieldWithFieldEquityPartners(1, "org A", "reg A"),
        getFieldWithFieldEquityPartners(2, "org B", "reg B"),
        getFieldWithFieldEquityPartners(3, "org C", "reg C")
    );

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion))
        .thenReturn(fields);

    consentFieldEquityPartnerService.saveFieldEquityPartners(consent, applicationVersion);

    verify(consentFieldEquityPartnerRepository).saveAll(consentFieldEquityPartnersArgumentCaptor.capture());

    // verify fields with field equity partners
    var firstFieldEquityPartner = fields.get(0).getFieldEquityPartners().get(0).getOrganisationUnit();
    var secondFieldEquityPartner = fields.get(1).getFieldEquityPartners().get(0).getOrganisationUnit();
    var thirdFieldEquityPartner = fields.get(2).getFieldEquityPartners().get(0).getOrganisationUnit();

    assertThat(consentFieldEquityPartnersArgumentCaptor.getAllValues().get(0))
        .extracting(
            ConsentFieldEquityPartner::getConsent,
            ConsentFieldEquityPartner::getOrganisationUnitId,
            ConsentFieldEquityPartner::getOrganisationName,
            ConsentFieldEquityPartner::getRegisteredNumber
        )
        .containsExactly(
            tuple(
                consent,
                firstFieldEquityPartner.getOrganisationUnitId(),
                firstFieldEquityPartner.getName(),
                firstFieldEquityPartner.getRegisteredNumber()),
            tuple(
                consent,
                secondFieldEquityPartner.getOrganisationUnitId(),
                secondFieldEquityPartner.getName(),
                secondFieldEquityPartner.getRegisteredNumber()),
            tuple(
                consent,
                thirdFieldEquityPartner.getOrganisationUnitId(),
                thirdFieldEquityPartner.getName(),
                thirdFieldEquityPartner.getRegisteredNumber())
        );
  }

  private Field getFieldWithFieldEquityPartners(Integer organisationUnitId, String name, String registeredNumber) {
    var fieldEquityPartners = List.of(
        FieldEquityPartner.newBuilder()
            .organisationUnit(OrganisationUnit.newBuilder()
                .organisationUnitId(organisationUnitId)
                .name(name)
                .registeredNumber(registeredNumber)
                .build())
            .build()
    );

    return Field.newBuilder()
        .fieldEquityPartners(fieldEquityPartners)
        .build();
  }

  @Test
  void getConsentFieldEquityPartnersByConsent_whenEmpty() {
    when(consentFieldEquityPartnerRepository.findAllByConsent(consent)).thenReturn(Collections.emptyList());
    assertThat(consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent)).isEmpty();
  }

  @Test
  void getConsentFieldEquityPartnersByConsent_whenNonEmpty() {
    var consentFieldEquityPartner1 = new ConsentFieldEquityPartner(consent, 1, "org A", "reg A");
    var consentFieldEquityPartner2 = new ConsentFieldEquityPartner(consent, 2, "org B", "reg B");
    var consentFieldEquityPartner3 = new ConsentFieldEquityPartner(consent, 3, "org C", "reg C");

    var consentFieldEquityPartners = List.of(consentFieldEquityPartner1, consentFieldEquityPartner2, consentFieldEquityPartner3);

    when(consentFieldEquityPartnerRepository.findAllByConsent(consent)).thenReturn(consentFieldEquityPartners);

    var actualFieldEquityPartners = consentFieldEquityPartnerService.getConsentFieldEquityPartnersByConsent(consent);

    assertThat(actualFieldEquityPartners).hasSize(3);
    assertThat(actualFieldEquityPartners.get(0)).usingRecursiveComparison().isEqualTo(consentFieldEquityPartner1);
    assertThat(actualFieldEquityPartners.get(1)).usingRecursiveComparison().isEqualTo(consentFieldEquityPartner2);
    assertThat(actualFieldEquityPartners.get(2)).usingRecursiveComparison().isEqualTo(consentFieldEquityPartner3);
  }
}
