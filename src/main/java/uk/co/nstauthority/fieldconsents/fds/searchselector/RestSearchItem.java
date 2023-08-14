package uk.co.nstauthority.fieldconsents.fds.searchselector;

/**
 * A RestSearchItem is used to produce each item within a search selector.
 * id and text are required fields for the JSON response.
 */
public record RestSearchItem(String id, String text) {
  public static final RestSearchItem EMPTY_REST_SEARCH_ITEM = new RestSearchItem("", "");

  public static RestSearchItem from(SearchSelectable searchSelectable) {
    return new RestSearchItem(searchSelectable.getSelectionId(), searchSelectable.getSelectionText());
  }

}
