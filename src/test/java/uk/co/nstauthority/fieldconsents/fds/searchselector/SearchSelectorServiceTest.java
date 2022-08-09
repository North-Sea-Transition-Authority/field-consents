package uk.co.nstauthority.fieldconsents.fds.searchselector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchSelectorServiceTest {

  private SearchSelectorService searchSelectorService;

  @BeforeEach
  void setUp() {
    searchSelectorService = new SearchSelectorService();
  }

  @Test
  void search_NoMatch() {
    List<SearchItem> searchableResults = List.of(
        new SearchItem(1, "fieldname")
    );
    List<RestSearchItem> result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result).isEmpty();
  }

  @Test
  void search_SearchableEmpty() {
    List<SearchSelectable> searchableResults = List.of();
    List<RestSearchItem> result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result).isEmpty();
  }

  @Test
  void search_Match() {
    SearchItem searchItem = new SearchItem(1, "fieldname");
    List<SearchItem> searchableResults = List.of(searchItem);
    List<RestSearchItem> result = searchSelectorService.search("fie", searchableResults);
    assertThat(result).extracting(RestSearchItem::id)
        .containsExactly(String.valueOf(searchItem.getId()));
  }

  @Test
  void addManualEntry() {
    List<RestSearchItem> searchableResults = searchSelectorService.addManualEntry("free_text", new ArrayList<>());
    assertThat(searchableResults).extracting(RestSearchItem::id)
        .containsExactly(SearchSelectablePrefix.FREE_TEXT_PREFIX + "free_text");
  }

  @Test
  void addManualEntry_noFreeText() {
    List<RestSearchItem> searchableResults = searchSelectorService.addManualEntry("free_text", new ArrayList<>(), ManualEntryAttribute.NO_FREE_TEXT_PREFIX);
    assertThat(searchableResults).extracting(RestSearchItem::id)
        .containsExactly("free_text");
  }

  @Test
  void buildPrepopulatedSelections() {
    String prefix = SearchSelectablePrefix.FREE_TEXT_PREFIX;
    List<String> selections = List.of(prefix + "Test", "1", "2");
    Map<String, String> resolvedMap = new HashMap<>() {{
      put("1", "One");
      put("2", "Two");
    }};
    Map<String, String> result = searchSelectorService.buildPrePopulatedSelections(selections, resolvedMap);
    assertThat(result).containsExactly(
        entry(prefix + "Test", "Test"),
        entry("1", "One"),
        entry("2", "Two")
    );
  }

  @Test
  void removePrefix() {
    String str = SearchSelectablePrefix.FREE_TEXT_PREFIX + "Test";
    assertThat(SearchSelectorService.removePrefix(str)).isEqualTo("Test");
  }

  //TODO re-add test once actual rest controller is created
//  @Test
//  void route() {
//    RestSearchResult routeOn = on(SearchSelectorTestController.class).search(null);
//    String route = SearchSelectorService.route(routeOn);
//    assertThat(route).doesNotEndWith("term");
//  }

  @Test
  void isManualEntry() {
    String manualEntry = SearchSelectablePrefix.FREE_TEXT_PREFIX + "manual_Entry";
    String notManualEntry = "123";
    assertThat(SearchSelectorService.isManualEntry(manualEntry)).isTrue();
    assertThat(SearchSelectorService.isManualEntry(notManualEntry)).isFalse();
  }

  @Test
  void getValueWithManualEntryPrefix() {
    String manualEntry = "manual entry";
    String result = SearchSelectorService.getValueWithManualEntryPrefix(manualEntry);
    assertThat(result).isEqualTo(SearchSelectablePrefix.FREE_TEXT_PREFIX + manualEntry);
  }

  @Test
  void getManualOrStandardSelection_withManualEntry() {
    String manual = "manual";
    String output = searchSelectorService.getManualOrStandardSelection(manual, null);
    assertThat(output).isEqualTo(SearchSelectorService.getValueWithManualEntryPrefix(manual));
  }

  @Test
  void getManualOrStandardSelection_withStandardEntry() {
    SearchSelectable standardEntry = new SearchSelectable() {
      @Override
      public String getSelectionId() {
        return "selectionId";
      }

      @Override
      public String getSelectionText() {
        return "selection text";
      }
    };
    String output = searchSelectorService.getManualOrStandardSelection(null, standardEntry);
    assertThat(output).isEqualTo(standardEntry.getSelectionId());
  }

  private static class SearchItem implements SearchSelectable {
    private final Integer id;
    private final String name;

    public SearchItem(Integer id,
                      String name) {
      this.id = id;
      this.name = name;
    }

    public Integer getId() { return id; }
    public String getName() { return name; }

    @Override
    public String getSelectionId() {
      return id.toString();
    }

    @Override
    public String getSelectionText() {
      return name;
    }
  }
}
