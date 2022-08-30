package uk.co.nstauthority.fieldconsents.fds.searchselector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
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
    RestSearchResult result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result.getResults()).isEmpty();
  }

  @Test
  void search_SearchableEmpty() {
    List<SearchSelectable> searchableResults = List.of();
    RestSearchResult result = searchSelectorService.search("should not match", searchableResults);
    assertThat(result.getResults()).isEmpty();
  }

  @Test
  void search_Match() {
    SearchItem searchItem = new SearchItem(1, "fieldname");
    List<SearchItem> searchableResults = List.of(searchItem);
    RestSearchResult result = searchSelectorService.search("fie", searchableResults);
    assertThat(result.getResults()).extracting(RestSearchItem::id)
        .containsExactly(String.valueOf(searchItem.getId()));
  }

  //TODO re-add test once actual rest controller is created
//  @Test
//  void route() {
//    RestSearchResult routeOn = on(SearchSelectorTestController.class).search(null);
//    String route = SearchSelectorService.route(routeOn);
//    assertThat(route).doesNotEndWith("term");
//  }

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
