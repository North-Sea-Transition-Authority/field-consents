package uk.co.nstauthority.fieldconsents.summary;

import java.util.List;

public record SummarySection(
    int displayOrder,
    List<SummaryItem> summaryItems
) {
}