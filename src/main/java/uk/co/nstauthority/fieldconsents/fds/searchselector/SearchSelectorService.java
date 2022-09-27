package uk.co.nstauthority.fieldconsents.fds.searchselector;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * A generic service to provide a list of RestSearchItems for any entities implementing SearchSelectable.
 */
@Service
public class SearchSelectorService {

  public RestSearchResult search(String searchQuery, Collection<? extends SearchSelectable> selectableList) {
    Collection<? extends SearchSelectable> results = selectableList.stream()
        .filter(searchSelectable ->
            searchSelectable.getSelectionText()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(searchQuery, "").toLowerCase()))
        .toList();

    return buildSearchResult(results);
  }

  public RestSearchResult search(String searchQuery, Function<String, Collection<? extends SearchSelectable>> searchFunction) {
    Collection<? extends SearchSelectable> results = searchFunction.apply(searchQuery);

    return buildSearchResult(results);
  }

  private RestSearchResult buildSearchResult(Collection<? extends SearchSelectable> selectableList) {

    List<RestSearchItem> results = selectableList.stream()
        .map(item -> new RestSearchItem(item.getSelectionId(), item.getSelectionText()))
        .toList();

    return new RestSearchResult(results);
  }

}
