import SelectableResultsAndActionsContainer from "./selectableResultsAndActionsContainer";
import TableWithPastableContent from "./tableWithPastableContent";
import RichTextEditor from "./richTextEditor";

const selectableResultsAndActionsContainer = document.querySelector("[data-module='fcs-selectable-results-and-actions-container']");
if (selectableResultsAndActionsContainer) {
  new SelectableResultsAndActionsContainer(selectableResultsAndActionsContainer);
}

for (const table of document.querySelectorAll("[data-module='fcs-table-with-pastable-content']")) {
  new TableWithPastableContent(table)
}

for (const element of document.querySelectorAll("[data-module='rich-text-editor']")) {
  new RichTextEditor(element);
}
