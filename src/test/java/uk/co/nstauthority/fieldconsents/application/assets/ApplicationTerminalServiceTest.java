package uk.co.nstauthority.fieldconsents.application.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.application.assets.ApplicationAssetTestUtil.terminalAsset1;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApplicationTerminalServiceTest {

  @Mock
  private ApplicationAssetService applicationAssetService;
  
  private ApplicationTerminalService applicationTerminalService;
  
  
  @BeforeEach
  void setUp() {
    applicationTerminalService = new ApplicationTerminalService(applicationAssetService);
  }

  @Test
  void findDistinctPrimaryTerminalIds_whenEmpty() {
    when(applicationAssetService.findAllPrimaryTerminalAssets()).thenReturn(Collections.emptyList());

    assertThat(applicationTerminalService.findDistinctPrimaryTerminalIds()).isEmpty();
  }

  @Test
  void findDistinctPrimaryTerminalIds_whenNotEmpty() {
    when(applicationAssetService.findAllPrimaryTerminalAssets()).thenReturn(List.of(terminalAsset1, terminalAsset1));

    var primaryTerminals = applicationTerminalService.findDistinctPrimaryTerminalIds();
    assertThat(primaryTerminals).hasSize(1);
    assertThat(primaryTerminals.get(0)).usingRecursiveComparison().isEqualTo(terminalAsset1.getTerminalId());
  }
}
