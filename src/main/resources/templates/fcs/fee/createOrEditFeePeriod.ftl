<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#include '../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  errorItems=errorList
>
  <@fdsForm.htmlForm>
    <@fdsDateInput.dateInput
      labelText="Start date"
      dayPath="form.startDateInput.dayInput.inputValue"
      monthPath="form.startDateInput.monthInput.inputValue"
      yearPath="form.startDateInput.yearInput.inputValue"
      formId="form.startDateInput"
      fieldsetHeadingClass="govuk-fieldset__legend--m"
    />

    <h2 class="govuk-!-margin-bottom-2 govuk-heading-m">Fee lines</h2>

    <table class="govuk-table govuk-!-width-full">
      <thead class="govuk-table__head">
        <tr class="govuk-table__row">
          <th class="govuk-table__header" scope="col">Asset type</th>
          <th class="govuk-table__header" scope="col">Consent type</th>
          <th class="govuk-table__header" scope="col">Duration</th>
          <th class="govuk-table__header" scope="col">New/Revised consent</th>
          <th class="govuk-table__header" scope="col">Cost</th>
        </tr>
      </thead>

      <tbody class="govuk-table__body">
        <#list feeLineViews as feeLineView>
          <tr class="govuk-table__row">
            <td class="govuk-table__cell">${feeLineView.mnemonic().assetType().getDisplayName()}</td>
            <td class="govuk-table__cell">${feeLineView.mnemonic().applicationType().getDisplayName()}</td>
            <td class="govuk-table__cell">${feeLineView.mnemonic().consentLengthType().getShortDisplayName()}</td>
            <td class="govuk-table__cell">${feeLineView.mnemonic().consentRevisionType().getDisplayName()}</td>
            <td class="govuk-table__cell">
              <@fdsTextInput.textInput
                path="form.feeLineAmountsByMnemonic[${feeLineView.mnemonic().mnemonic()}]"
                inputClass="govuk-input--width-7"
                formGroupClass="govuk-!-margin-bottom-0"
                labelText="Cost for ${feeLineView.mnemonic().assetType().getDisplayName()} ${feeLineView.mnemonic().applicationType().getDisplayName()} ${feeLineView.mnemonic().consentLengthType().getDisplayName()} ${feeLineView.mnemonic().consentRevisionType().getDisplayName()}"
                labelClass="govuk-visually-hidden"
                prefix="£"
              />
            </td>
          </tr>
        </#list>
      </tbody>
    </table>

    <@fdsAction.submitButtons
      primaryButtonText="${submitButtonText}"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
