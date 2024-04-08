package uk.co.nstauthority.fieldconsents.application.fieldequitypartner;

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
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil;
import uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.Consent;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

@ExtendWith(MockitoExtension.class)
class ConsentFieldEquityPartnerServiceTest {

  @Mock
  private FieldEquityPartnerService fieldEquityPartnerService;

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private ConsentFieldEquityPartnerRepository consentFieldEquityPartnerRepository;

  @Captor
  private ArgumentCaptor<List<ConsentFieldEquityPartner>> consentFieldEquityPartnersArgumentCaptor;

  private ConsentFieldEquityPartnerService consentFieldEquityPartnerService;

  private ApplicationVersion applicationVersion;

  @BeforeEach
  void setUp() {
    applicationVersion = ApplicationTestUtil.getSubmittedApplicationVersionWithType(ApplicationType.PRODUCTION);

    consentFieldEquityPartnerService = new ConsentFieldEquityPartnerService(
        applicationAssetService,
        fieldEquityPartnerService,
        consentFieldEquityPartnerRepository
    );
  }

  @Test
  void saveFieldEquityPartners_whenApplicationIsForTerminal() {
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.TERMINAL).build();

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    consentFieldEquityPartnerService.saveFieldEquityPartners(new Consent(), applicationVersion);

    verify(consentFieldEquityPartnerRepository, never()).save(any());
  }

  @Test
  void saveFieldEquityPartners_whenApplicationIsForFieldButNoFieldEquityPartnersAreFound_thenThrowException() {
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion))
        .thenReturn(Collections.emptyList());

    var consent = new Consent(1);
    assertThatThrownBy(() -> consentFieldEquityPartnerService.saveFieldEquityPartners(consent, applicationVersion))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("No fields with field equity partners found for application version [1]");

    verify(consentFieldEquityPartnerRepository, never()).save(any());
  }

  @Test
  void saveFieldEquityPartners_whenApplicationIsForField_thenFieldEquityPartnersAreSaved() {
    var primaryApplicationAsset = ApplicationAssetTestUtil.newBuilder().withAssetType(AssetType.FIELD).build();

    when(applicationAssetService.getPrimaryAsset(applicationVersion)).thenReturn(primaryApplicationAsset);

    var fields = List.of(
        getFieldWithFieldEquityPartners(1, "org A", "reg A"),
        getFieldWithFieldEquityPartners(2, "org B", "reg B"),
        getFieldWithFieldEquityPartners(3, "org C", "reg C")
    );

    when(fieldEquityPartnerService.getFieldsWithFieldEquityPartners(applicationVersion))
        .thenReturn(fields);

    var consent = new Consent(1);
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
}
