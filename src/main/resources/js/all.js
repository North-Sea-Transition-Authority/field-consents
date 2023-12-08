import SelectableResultsAndActionsContainer from "./selectableResultsAndActionsContainer";

const selectableResultsAndActionsContainer = document.querySelector("[data-module='fcs-selectable-results-and-actions-container']");
if (selectableResultsAndActionsContainer) {
  new SelectableResultsAndActionsContainer(selectableResultsAndActionsContainer);
}
