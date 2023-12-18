import announce from "../templates/fds/js/accessibility";

export default class TableWithPastableContent {

  constructor(table) {
    this.defaultOffsetX = 1;
    this.defaultOffsetY = 1;
    this.pastedClassName = "pasted";

    table.addEventListener("paste", (e) => {
      const text = (e.clipboardData || window.clipboardData).getData("text");
      if (!text) {
        return;
      }

      e.preventDefault();

      const [offsetX, offsetY] = this.getTargetOffsetWithinTable(table, e.target);
      this.pasteTextIntoTable(text, table, offsetX, offsetY);
    });
  }

  pasteTextIntoTable(text, table, offsetX, offsetY) {
    const tableRows = table.querySelectorAll("tr");
    const inputRows = text.split("\n");

    let pastedRows = 0;
    let pastedColumns = 0;

    for (let y = 0; y < Math.min(tableRows.length - offsetY, inputRows.length); y++) {
      const tableColumns = tableRows[y + offsetY].querySelectorAll("td");
      const inputColumns = inputRows[y].split("\t");

      pastedRows++;

      const columnsToPaste = Math.min(tableColumns.length - offsetX, inputColumns.length);
      let pastedColumnsToRow = 0;

      for (let x = 0; x < columnsToPaste; x++) {
        const pasteTarget = tableColumns[x + offsetX].querySelector("input, textarea");

        if (pasteTarget.readOnly || pasteTarget.disabled) {
          continue;
        }

        pastedColumnsToRow++

        this.updateTargetValue(pasteTarget, inputColumns[x]);
      }

      pastedColumns = Math.max(pastedColumnsToRow, pastedColumns);
    }

    this.announcePastedDetails(pastedRows, pastedColumns);
  }

  updateTargetValue(target, newValue) {
    target.value = newValue;
    target.classList.add(this.pastedClassName);
    setTimeout(() => target.classList.remove(this.pastedClassName), 500);
  }

  announcePastedDetails(pastedRows, pastedColumns) {
    if (pastedRows > 0 || pastedColumns > 0) {
      const row = pastedRows > 1 ? "rows" : "row";
      const column = pastedColumns > 1 ? "columns" : "column";

      announce(`Pasted ${pastedRows} ${row} and ${pastedColumns} ${column}`);
      return;
    }

    announce("No data was pasted");
  }

  getTargetOffsetWithinTable(table, target) {
    const targetTagName = target.tagName.toLowerCase();
    if (targetTagName !== "input" && targetTagName !== "textarea") {
      return [this.defaultOffsetX, this.defaultOffsetY];
    }

    const rows = table.querySelectorAll("tr");
    for (let y = this.defaultOffsetY; y < rows.length; y++) {
      const columns = rows[y].querySelectorAll("td");
      for (let x = this.defaultOffsetX; x < columns.length; x++) {
        if (columns[x].isEqualNode(target.closest("td"))) {
          return [x, y];
        }
      }
    }

    return [this.defaultOffsetX, this.defaultOffsetY];
  }

}
