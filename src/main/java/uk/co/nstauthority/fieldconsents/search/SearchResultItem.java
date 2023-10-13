package uk.co.nstauthority.fieldconsents.search;

import uk.co.nstauthority.fieldconsents.query.ApplicationDataItem;

public record SearchResultItem(ApplicationDataItem applicationDataItem, String licenses) {

}
