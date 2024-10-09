package uk.co.nstauthority.fieldconsents.assets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3Json;
import static uk.co.nstauthority.fieldconsents.assets.fields.FieldTestUtil.field3JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1Json;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal1JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal2JsonWithOperator;
import static uk.co.nstauthority.fieldconsents.assets.terminals.TerminalTestUtil.terminal3JsonWithOperator;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.nstauthority.fieldconsents.assets.fields.FieldSearchService;
import uk.co.nstauthority.fieldconsents.assets.terminals.TerminalSearchService;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetailTestUtil;

@ExtendWith(MockitoExtension.class)
class AssetSearchServiceTest {

  private static final String SEARCH_FIELDS_PURPOSE = "Assets search selector (search fields)";
  private static final String SEARCH_TERMINALS_PURPOSE = "Assets search selector (search terminals)";

  @Mock
  private FieldSearchService fieldSearchService;

  @Mock
  private TerminalSearchService terminalSearchService;

  @InjectMocks
  private AssetSearchService assetSearchService;

  private final ServiceUserDetail user = ServiceUserDetailTestUtil.Builder().build();

  @Test
  void searchFieldsAndTerminalsForUser_verifyListAndOrder() {
    when(fieldSearchService
        .searchFieldsWithOperatorForUser("1", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of(field1JsonWithOperator));
    when(terminalSearchService
        .searchTerminalsWithOperatorForUser("1", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of(terminal1JsonWithOperator));

    var searchAssetsResults = assetSearchService.searchAssetsForUser("1", user);

    assertThat(searchAssetsResults).containsExactly(
        field1JsonWithOperator,
        terminal1JsonWithOperator);
  }

  @Test
  void searchFieldsAndTerminalsForUser_verifyListAndOrderFieldsOnly() {
    when(fieldSearchService
        .searchFieldsWithOperatorForUser("F", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of(field3JsonWithOperator, field1JsonWithOperator, field2JsonWithOperator));
    when(terminalSearchService
        .searchTerminalsWithOperatorForUser("F", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of());

    var searchAssetsResults = assetSearchService.searchAssetsForUser("F", user);

    assertThat(searchAssetsResults).containsExactly(
        field1JsonWithOperator,
        field2JsonWithOperator,
        field3JsonWithOperator);
  }

  @Test
  void searchFieldsAndTerminalsForUser_verifyListAndOrderTerminalsOnly() {
    when(fieldSearchService
        .searchFieldsWithOperatorForUser("T", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of());
    when(terminalSearchService
        .searchTerminalsWithOperatorForUser("T", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of(terminal2JsonWithOperator, terminal3JsonWithOperator, terminal1JsonWithOperator));

    var searchAssetsResults = assetSearchService.searchAssetsForUser("T", user);

    assertThat(searchAssetsResults).containsExactly(
        terminal1JsonWithOperator,
        terminal2JsonWithOperator,
        terminal3JsonWithOperator);
  }

  @Test
  void searchFieldsForUser_whenNotFound() {
    when(fieldSearchService
        .searchFieldsWithOperatorForUser("F", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of());

    var searchAssetsResults = assetSearchService.searchFieldsForUser("F", user);

    assertThat(searchAssetsResults).isEmpty();
  }

  @Test
  void searchFieldsForUser_whenFound() {
    when(fieldSearchService
        .searchFieldsWithOperatorForUser("F", SEARCH_FIELDS_PURPOSE, user))
        .thenReturn(List.of(field1JsonWithOperator, field2JsonWithOperator, field3JsonWithOperator));

    var searchAssetsResults = assetSearchService.searchFieldsForUser("F", user);

    assertThat(searchAssetsResults).containsExactly(
        field1JsonWithOperator,
        field2JsonWithOperator,
        field3JsonWithOperator);
  }

  @Test
  void searchTerminalsForUser_whenNotFound() {
    when(terminalSearchService
        .searchTerminalsWithOperatorForUser("T", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of());

    var searchAssetsResults = assetSearchService.searchTerminalsForUser("T", user);

    assertThat(searchAssetsResults).isEmpty();
  }

  @Test
  void searchTerminalsForUser_whenFound() {
    when(terminalSearchService
        .searchTerminalsWithOperatorForUser("T", SEARCH_TERMINALS_PURPOSE, user))
        .thenReturn(List.of(terminal1JsonWithOperator, terminal2JsonWithOperator, terminal3JsonWithOperator));

    var searchAssetsResults = assetSearchService.searchTerminalsForUser("T", user);

    assertThat(searchAssetsResults).containsExactly(
        terminal1JsonWithOperator,
        terminal2JsonWithOperator,
        terminal3JsonWithOperator);
  }

  @Test
  void searchFieldsAndTerminals_verifyListAndOrder() {
    when(fieldSearchService.searchFields("1", SEARCH_FIELDS_PURPOSE)).thenReturn(List.of(field1Json));
    when(terminalSearchService.searchTerminals("1", SEARCH_TERMINALS_PURPOSE)).thenReturn(List.of(terminal1Json));

    assertThat(assetSearchService.searchFieldsAndTerminals("1")).containsExactly(field1Json, terminal1Json);
  }

  @Test
  void searchFieldsAndTerminals_verifyListAndOrderFieldsOnly() {
    when(fieldSearchService.searchFields("1", SEARCH_FIELDS_PURPOSE)).thenReturn(List.of(field1Json));
    when(terminalSearchService.searchTerminals("1", SEARCH_TERMINALS_PURPOSE)).thenReturn(Collections.emptyList());

    assertThat(assetSearchService.searchFieldsAndTerminals("1")).containsExactly(field1Json);
  }

  @Test
  void searchFieldsAndTerminals_verifyListAndOrderTerminalsOnly() {
    when(fieldSearchService.searchFields("1", SEARCH_FIELDS_PURPOSE)).thenReturn(Collections.emptyList());
    when(terminalSearchService.searchTerminals("1", SEARCH_TERMINALS_PURPOSE)).thenReturn(List.of(terminal1Json));

    assertThat(assetSearchService.searchFieldsAndTerminals("1")).containsExactly(terminal1Json);
  }

  @Test
  void searchFields_verifyListAndOrderFieldsOnly() {
    when(fieldSearchService.searchFields("F", SEARCH_FIELDS_PURPOSE))
        .thenReturn(List.of(field1Json, field2Json, field3Json));

    List<AssetJson> searchAssetsResults = assetSearchService.searchFields("F");

    assertThat(searchAssetsResults).containsExactly(
        field1Json,
        field2Json,
        field3Json
    );
  }
}
