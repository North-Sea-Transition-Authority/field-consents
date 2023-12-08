export default class SelectableResultsAndActionsContainer {

  constructor(module) {
    const checkboxes = module.querySelectorAll("ol.fds-result-list input[type='checkbox']")

    module.querySelector(".selectAllResults").addEventListener("click", () =>
      checkboxes.forEach(checkbox => this.setCheckboxValue(checkbox, true))
    );

    module.querySelector(".deSelectAllResults").addEventListener("click", () =>
      checkboxes.forEach(checkbox => this.setCheckboxValue(checkbox, false))
    );
  }

  setCheckboxValue(checkbox, value) {
    if (checkbox.checked === value) {
      return
    }

    checkbox.checked = value
  }

}
