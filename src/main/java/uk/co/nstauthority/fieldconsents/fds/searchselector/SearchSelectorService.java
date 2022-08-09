package uk.co.nstauthority.fieldconsents.fds.searchselector;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import uk.co.nstauthority.fieldconsents.mvc.ReverseRouter;

/**
 * A generic service to provide a list of RestSearchItems for any entities implementing SearchSelectable.
 * An optional {@link #addManualEntry} method is provided if the endpoint requires manually entered text.
 */
@Service
public class SearchSelectorService {

  public List<RestSearchItem> search(String searchQuery, Collection<? extends SearchSelectable> selectableList) {
    return selectableList.stream()
        .filter(searchSelectable ->
            searchSelectable.getSelectionText()
                .toLowerCase()
                .contains(StringUtils.defaultIfBlank(searchQuery, "").toLowerCase()))
        .map(item -> new RestSearchItem(item.getSelectionId(), item.getSelectionText()))
        .toList();
  }

  public RestSearchResult getSearchResultsWithManualEntry(String searchTerm, List<? extends SearchSelectable> searchableList) {
    List<RestSearchItem> results = search(searchTerm, searchableList)
        .stream()
        .sorted(Comparator.comparing(RestSearchItem::text))
        .toList();
    addManualEntry(searchTerm, results);
    return new RestSearchResult(results);
  }

  public List<RestSearchItem> addManualEntry(String searchQuery, List<RestSearchItem> resultList) {
    return addManualEntry(searchQuery, resultList, ManualEntryAttribute.WITH_FREE_TEXT_PREFIX);
  }

  public List<RestSearchItem> addManualEntry(String searchQuery, List<RestSearchItem> resultList,
                                             ManualEntryAttribute manualEntryAttribute) {
    if (!StringUtils.isBlank(searchQuery)) {
      boolean entryExists = resultList.stream()
          .anyMatch(restSearchItem -> restSearchItem.text().equalsIgnoreCase(searchQuery));
      if (!entryExists) {
        if (manualEntryAttribute.equals(ManualEntryAttribute.WITH_FREE_TEXT_PREFIX)) {
          resultList.add(0, new RestSearchItem(SearchSelectablePrefix.FREE_TEXT_PREFIX + searchQuery, searchQuery));
        } else {
          resultList.add(0, new RestSearchItem(searchQuery, searchQuery));
        }
      }
    }
    return resultList;
  }

  public static String route(Object methodCall) {
    return StringUtils.replace(ReverseRouter.route(methodCall), "?term", "");
  }

  /**
   * Build a map of manual entries and linked entries, with the linked entry display text.
   *
   * @param selections             All selected items from a form field.
   * @param resolvedLinkedEntryMap A map of ID (String) -> DisplayText (String).
   * @return A map of selection results to pre-populate the search selector.
   */
  public Map<String, String> buildPrePopulatedSelections(List<String> selections,
                                                         Map<String, String> resolvedLinkedEntryMap) {
    Map<String, String> results = new LinkedHashMap<>();
    for (String s : selections) {
      if (s.startsWith(SearchSelectablePrefix.FREE_TEXT_PREFIX)) {
        results.put(s, removePrefix(s));
      } else {
        results.put(s, resolvedLinkedEntryMap.get(s));
      }
    }
    return results;
  }

  public static String removePrefix(String s) {
    return StringUtils.substring(s, SearchSelectablePrefix.FREE_TEXT_PREFIX.length());
  }

  public static boolean isManualEntry(String s) {
    return s != null && s.startsWith(SearchSelectablePrefix.FREE_TEXT_PREFIX);
  }

  public static String getValueWithManualEntryPrefix(String value) {
    return SearchSelectablePrefix.FREE_TEXT_PREFIX  + value;
  }

  public String getManualOrStandardSelection(String manualSelection, SearchSelectable standardSelection) {
    String output = null;
    if (manualSelection != null) {
      output = SearchSelectorService.getValueWithManualEntryPrefix(manualSelection);
    } else if (standardSelection != null) {
      output = standardSelection.getSelectionId();
    }
    return output;
  }
}
