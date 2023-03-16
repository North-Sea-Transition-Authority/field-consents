package uk.co.nstauthority.fieldconsents.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAsset;
import uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetService;
import uk.co.nstauthority.fieldconsents.application.assets.AssetRole;
import uk.co.nstauthority.fieldconsents.assets.AssetJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitJson;
import uk.co.nstauthority.fieldconsents.organisations.OrganisationUnitService;
import uk.co.nstauthority.fieldconsents.summary.SummaryCard;
import uk.co.nstauthority.fieldconsents.summary.SummaryKeyValue;

@ExtendWith(MockitoExtension.class)
class ApplicationContextServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;

  @Mock
  private OrganisationUnitService organisationUnitService;

  private ApplicationContextService applicationContextService;

  private ApplicationVersion applicationVersion;

  private ApplicationAsset primaryApplicationAsset;

  private OrganisationUnitJson primaryOperator;

  @BeforeEach
  void setUp() {
    applicationContextService = new ApplicationContextService(
        applicationAssetService,
        organisationUnitService
    );
    applicationVersion = ApplicationTestUtil.getApplicationVersionWithType(ApplicationType.FLARE);

    primaryApplicationAsset = new ApplicationAsset();
    primaryApplicationAsset.setApplicationVersion(applicationVersion);
    primaryApplicationAsset.setAssetRole(AssetRole.PRIMARY);
    when(applicationAssetService.getPrimaryAsset(applicationVersion))
        .thenReturn(primaryApplicationAsset);

    primaryOperator = new OrganisationUnitJson(applicationVersion.getPrimaryOperatorOuId(), applicationVersion.getCachedPrimaryOperatorName());
  }

  @Test
  void getApplicationContextJson_operatorFound_terminalApp() {
    AssetJson primaryAsset = terminal1Json;
    when(applicationAssetService.getAssetJsonForApplicationAsset(primaryApplicationAsset))
        .thenReturn(primaryAsset);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()), any(), eq(applicationVersion.getCachedPrimaryOperatorName())))
        .thenReturn(primaryOperator);

    ApplicationContextJson applicationContextJson
        = applicationContextService.getApplicationContextJson(applicationVersion);

    assertThat(applicationContextJson)
        .isEqualTo(new ApplicationContextJson(primaryAsset, primaryOperator));

    assertThat(applicationContextJson)
        .extracting(ApplicationContextJson::getPrimaryAssetPrompt,
            ApplicationContextJson::getPrimaryAssetName,
            ApplicationContextJson::getPrimaryOperatorName)
        .containsExactly("Primary facility",
            primaryAsset.getName(),
            primaryOperator.name());
  }

  @Test
  void getApplicationContextJson_operatorCacheUsed_fieldApp() {
    AssetJson primaryAsset = field1Json;
    when(applicationAssetService.getAssetJsonForApplicationAsset(primaryApplicationAsset))
        .thenReturn(primaryAsset);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()), any(), eq(applicationVersion.getCachedPrimaryOperatorName())))
        .thenReturn(primaryOperator);

    ApplicationContextJson applicationContextJson
        = applicationContextService.getApplicationContextJson(applicationVersion);

    assertThat(applicationContextJson)
        .isEqualTo(new ApplicationContextJson(primaryAsset, primaryOperator));

    assertThat(applicationContextJson)
        .extracting(ApplicationContextJson::getPrimaryAssetPrompt,
            ApplicationContextJson::getPrimaryAssetName,
            ApplicationContextJson::getPrimaryOperatorName)
        .containsExactly("Primary field",
            primaryAsset.getName(),
            primaryOperator.name());
  }

  @Test
  void getApplicationContextSummaryCard_terminal() {
    AssetJson primaryAsset = terminal1Json;
    when(applicationAssetService.getAssetJsonForApplicationAsset(primaryApplicationAsset))
        .thenReturn(primaryAsset);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()), any(), eq(applicationVersion.getCachedPrimaryOperatorName())))
        .thenReturn(primaryOperator);

    var summaryCard = applicationContextService.getApplicationContextSummaryCard(applicationVersion);

    assertThat(summaryCard).usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            List.of(
                new SummaryKeyValue("Application type", applicationVersion.getApplication().getType().getDisplayName()),
                new SummaryKeyValue("Primary facility", primaryAsset.getName()),
                new SummaryKeyValue("Primary operator", primaryOperator.name())
            )));

  }

  @Test
  void getApplicationContextSummaryCard_field() {
    AssetJson primaryAsset = field1Json;
    when(applicationAssetService.getAssetJsonForApplicationAsset(primaryApplicationAsset))
        .thenReturn(primaryAsset);

    when(organisationUnitService.getOrganisationUnitByIdOrFallback(
        eq(applicationVersion.getPrimaryOperatorOuId()), any(), eq(applicationVersion.getCachedPrimaryOperatorName())))
        .thenReturn(primaryOperator);

    var summaryCard = applicationContextService.getApplicationContextSummaryCard(applicationVersion);

    assertThat(summaryCard).usingRecursiveComparison()
        .isEqualTo(SummaryCard.simpleSummaryCard(
            List.of(
                new SummaryKeyValue("Application type", applicationVersion.getApplication().getType().getDisplayName()),
                new SummaryKeyValue("Primary field", primaryAsset.getName()),
                new SummaryKeyValue("Primary operator", primaryOperator.name())
            )));

  }
}
