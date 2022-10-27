package uk.co.nstauthority.fieldconsents.startapplication;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.co.nstauthority.fieldconsents.application.ApplicationType;
import uk.co.nstauthority.fieldconsents.assets.AssetType;

class StartApplicationControllerHelperServiceTest {

  private StartApplicationControllerHelperService helperService;

  Map<String, String> expectedMapForFieldType;

  Map<String, String> expectedMapForTerminalType;

  @BeforeEach
  void setUp() {
    helperService = new StartApplicationControllerHelperService();

    expectedMapForFieldType = populateFieldMapType();
    expectedMapForTerminalType = populateTerminalMapType();
  }

  @Test
  void getApplicationTypesMap_fieldAssetType() {
    Map<String, String> actualMapForFieldType = helperService.getApplicationTypesMap(AssetType.FIELD);
    assertThat(actualMapForFieldType.size()).isEqualTo(3);
    assertThat(actualMapForFieldType.get(ApplicationType.PRODUCTION.name())).isEqualTo(ApplicationType.PRODUCTION.getDisplayName());
    assertThat(actualMapForFieldType.get(ApplicationType.FLARE.name())).isEqualTo(ApplicationType.FLARE.getDisplayName());
    assertThat(actualMapForFieldType.get(ApplicationType.VENT.name())).isEqualTo(ApplicationType.VENT.getDisplayName());
  }

  @Test
  void getApplicationTypesMap_terminalAssetType() {
    Map<String, String> actualMapForTerminalType = helperService.getApplicationTypesMap(AssetType.TERMINAL);
    assertThat(actualMapForTerminalType.size()).isEqualTo(2);
    assertThat(actualMapForTerminalType.get(ApplicationType.FLARE.name())).isEqualTo(ApplicationType.FLARE.getDisplayName());
    assertThat(actualMapForTerminalType.get(ApplicationType.VENT.name())).isEqualTo(ApplicationType.VENT.getDisplayName());
  }

  private static Map<String, String> populateFieldMapType() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put(ApplicationType.PRODUCTION.name(), ApplicationType.PRODUCTION.getDisplayName());
    map.put(ApplicationType.FLARE.name(), ApplicationType.FLARE.getDisplayName());
    map.put(ApplicationType.VENT.name(), ApplicationType.VENT.getDisplayName());
    return map;
  }

  private static Map<String, String> populateTerminalMapType() {
    Map<String, String> map = new LinkedHashMap<>();
    map.put(ApplicationType.FLARE.name(), ApplicationType.FLARE.getDisplayName());
    map.put(ApplicationType.VENT.name(), ApplicationType.VENT.getDisplayName());
    return map;
  }
}