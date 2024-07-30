import SelectableResultsAndActionsContainer from "./selectableResultsAndActionsContainer";
import TableWithPastableContent from "./tableWithPastableContent";
import RichTextEditor from "./richTextEditor";
import StackedBarChart from "./highcharts/stackedBarChart";

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

for (const element of document.querySelectorAll("[data-module='fcs-stacked-bar-chart']")) {
  new StackedBarChart(element);
}
