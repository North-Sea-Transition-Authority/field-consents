import announce from "../templates/fds/js/accessibility";

export default class SelectableResultsAndActionsContainer {

  constructor(module) {
    const checkboxes = module.querySelectorAll("ol.fds-result-list input[type='checkbox']")

    module.querySelector(".selectAllResults").addEventListener("click", () => {
        checkboxes.forEach(checkbox => this.setCheckboxValue(checkbox, true));
        announce(`All applications selected (${checkboxes.length} in total)`);
      }
    );

    module.querySelector(".deSelectAllResults").addEventListener("click", () => {
        checkboxes.forEach(checkbox => this.setCheckboxValue(checkbox, false));
        announce(`No applications selected`);
      }
    );
  }

  setCheckboxValue(checkbox, value) {
    if (checkbox.checked === value) {
      return
    }

    checkbox.checked = value
  }

}
