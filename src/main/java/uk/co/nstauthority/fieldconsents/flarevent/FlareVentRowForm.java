package uk.co.nstauthority.fieldconsents.flarevent;

import uk.co.fivium.formlibrary.input.DecimalInput;
import uk.co.nstauthority.fieldconsents.formatting.DecimalFormatUtils;

public class FlareVentRowForm {

  private final DecimalInput categoryA;

  private final DecimalInput categoryB;

  private final DecimalInput categoryC;

  public FlareVentRowForm() {
    categoryA = new DecimalInput("categoryA", "Category A");
    categoryB = new DecimalInput("categoryB", "Category B");
    categoryC = new DecimalInput("categoryC", "Category C");
  }

  public DecimalInput getCategoryA() {
    return categoryA;
  }

  public void setCategoryA(String categoryA) {
    this.categoryA.setInputValue(categoryA);
  }

  public DecimalInput getCategoryB() {
    return categoryB;
  }

  public void setCategoryB(String categoryB) {
    this.categoryB.setInputValue(categoryB);
  }

  public DecimalInput getCategoryC() {
    return categoryC;
  }

  public void setCategoryC(String categoryC) {
    this.categoryC.setInputValue(categoryC);
  }

  public void updateFromFlareVentRow(FlareVentRow flareVentRow) {
    setCategoryA(DecimalFormatUtils.bigDecimalToFormattedString(flareVentRow.getCategoryA()));
    setCategoryB(DecimalFormatUtils.bigDecimalToFormattedString(flareVentRow.getCategoryB()));
    setCategoryC(DecimalFormatUtils.bigDecimalToFormattedString(flareVentRow.getCategoryC()));
  }
}
