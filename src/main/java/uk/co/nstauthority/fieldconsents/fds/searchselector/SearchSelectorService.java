package uk.co.nstauthority.fieldconsents.fds.searchselector;

import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * A generic service to provide a list of RestSearchItems for any entities implementing SearchSelectable.
 */
@Service
public class SearchSelectorService {

  public RestSearchResult search(String searchQuery, Collection<? extends SearchSelectable> selectableList) {
    List<RestSearchItem> results = selectableList.stream()
        .filter(searchSelectable ->
            searchSelectable.getSelectionText()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(searchQuery, "").toLowerCase()))
        .map(item -> new RestSearchItem(item.getSelectionId(), item.getSelectionText()))
        .toList();

    return new RestSearchResult(results);
  }
}
