<#include '../../../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  backLinkWithBrowserBack=true
>
  <@fdsForm.htmlForm>
    <@fdsDateInput.dateInput
      labelText="Consent start date"
      formId="form.consentStartDateInput"
      dayPath="form.consentStartDateInput.dayInput.inputValue"
      monthPath="form.consentStartDateInput.monthInput.inputValue"
      yearPath="form.consentStartDateInput.yearInput.inputValue"
    />
    <@fdsDateInput.dateInput
      labelText="Consent end date"
      formId="form.consentEndDateInput"
      dayPath="form.consentEndDateInput.dayInput.inputValue"
      monthPath="form.consentEndDateInput.monthInput.inputValue"
      yearPath="form.consentEndDateInput.yearInput.inputValue"
    />

    <#if applicationType.name() == "PRODUCTION">
      <h2 class="govuk-heading-s">Consent figures</h2>

      <table class="govuk-table">
        <thead class="govuk-table__head">
          <tr class="govuk-table__row">
            <#if consentLengthType.name() == "LONG_TERM">
              <th class="govuk-table__header">Year</th>
            </#if>
            <th class="govuk-table__header">Minimum oil (${consentFigureUnitView.productionOilUnit().getDisplayName()})</th>
            <th class="govuk-table__header">Maximum oil (${consentFigureUnitView.productionOilUnit().getDisplayName()})</th>
            <th class="govuk-table__header">Minimum gas (${consentFigureUnitView.productionGasUnit().getDisplayName()})</th>
            <th class="govuk-table__header">Maximum gas (${consentFigureUnitView.productionGasUnit().getDisplayName()})</th>
          </tr>
        </thead>
        <tbody class="govuk-table__body">
          <#if consentLengthType.name() == "SHORT_TERM" || consentLengthType.name() == "ANNUAL">
            <tr class="govuk-table__row">
              <@consentProductionFiguresCells
                path="form.shortTermOrAnnualConsentProductionFiguresInput"
                consentProductionFiguresInput=form.shortTermOrAnnualConsentProductionFiguresInput
              />
            </tr>
          <#elseif consentLengthType.name() == "LONG_TERM">
            <#list form.longTermConsentProductionFiguresInputs as year, consentProductionFiguresInput>
              <tr class="govuk-table__row">
                <td class="govuk-table__cell">${year}</td>
                <@consentProductionFiguresCells
                  path="form.longTermConsentProductionFiguresInputs[${year}]"
                  consentProductionFiguresInput=consentProductionFiguresInput
                />
              </tr>
            </#list>
          </#if>
        </tbody>
      </table>
    <#elseif applicationType.name() == "FLARE" || applicationType.name() == "VENT">
      <@fdsTextInput.textInput
        path="form.emissionDailyAverageInput.inputValue"
        labelText="Daily average (${consentFigureUnitView.emissionAverageUnit().getDisplayName()})"
        inputClass="govuk-input--width-4"
      />
    </#if>

    <@fdsAction.button buttonText="Save"/>
  </@fdsForm.htmlForm>
</@defaultPage>

<#macro consentProductionFiguresCells path consentProductionFiguresInput>
  <td class="govuk-table__cell">
    <@fdsTextInput.textInput
      path="${path}.minOilInput.inputValue"
      labelText="${consentProductionFiguresInput.minOilInput.displayName}"
      formGroupClass="govuk-!-margin-bottom-0"
      labelClass="govuk-visually-hidden"
    />
  </td>
  <td class="govuk-table__cell">
    <@fdsTextInput.textInput
      path="${path}.maxOilInput.inputValue"
      labelText="${consentProductionFiguresInput.maxOilInput.displayName}"
      formGroupClass="govuk-!-margin-bottom-0"
      labelClass="govuk-visually-hidden"
    />
  </td>
  <td class="govuk-table__cell">
    <@fdsTextInput.textInput
      path="${path}.minGasInput.inputValue"
      labelText="${consentProductionFiguresInput.minGasInput.displayName}"
      formGroupClass="govuk-!-margin-bottom-0"
      labelClass="govuk-visually-hidden"
    />
  </td>
  <td class="govuk-table__cell">
    <@fdsTextInput.textInput
      path="${path}.maxGasInput.inputValue"
      labelText="${consentProductionFiguresInput.maxGasInput.displayName}"
      formGroupClass="govuk-!-margin-bottom-0"
      labelClass="govuk-visually-hidden"
    />
  </td>
</#macro>
