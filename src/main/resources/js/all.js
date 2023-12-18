import SelectableResultsAndActionsContainer from "./selectableResultsAndActionsContainer";
import TableWithPastableContent from "./tableWithPastableContent";

const selectableResultsAndActionsContainer = document.querySelector("[data-module='fcs-selectable-results-and-actions-container']");
if (selectableResultsAndActionsContainer) {
  new SelectableResultsAndActionsContainer(selectableResultsAndActionsContainer);
}

for (const table of document.querySelectorAll("[data-module='fcs-table-with-pastable-content']")) {
  new TableWithPastableContent(table)
}
