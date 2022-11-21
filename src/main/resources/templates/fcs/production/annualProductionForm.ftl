<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Annual Oil and Gas Production Request ${requestYear}"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
      <input type="hidden" name="year" value="${requestYear}"/>
      <table class="govuk-table">
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header govuk-!-width-one-third">Month</th>
          <th class="govuk-table__header govuk-!-width-one-third">Minimum Oil (${oilUnit})</th>
          <th class="govuk-table__header govuk-!-width-one-third">Maximum Oil (${oilUnit})</th>
          <th class="govuk-table__header govuk-!-width-one-third">Minimum Gas (${gasUnit})</th>
          <th class="govuk-table__header govuk-!-width-one-third">Maximum Gas (${gasUnit})</th>
        </tr>
          <#list form.annualProductionMonthForms as monthForm>
            <tr class="govuk-table__row">
              <@spring.bind "form.annualProductionMonthForms[${monthForm_index}].month"/>
              <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <@spring.bind "form.annualProductionMonthForms[${monthForm_index}].oilMinUnit"/>
              <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <@spring.bind "form.annualProductionMonthForms[${monthForm_index}].oilMaxUnit"/>
              <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <@spring.bind "form.annualProductionMonthForms[${monthForm_index}].gasMinUnit"/>
              <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <@spring.bind "form.annualProductionMonthForms[${monthForm_index}].gasMaxUnit"/>
              <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <td class="govuk-table__cell">${monthForm.month}</td>
              <td class="govuk-table__cell">
                  <@fdsTextInput.textInput path="form.annualProductionMonthForms[${monthForm_index}].oilMinValue.inputValue" labelText="Oil min value" labelClass="govuk-visually-hidden"/>
              </td>
              <td class="govuk-table__cell">
                  <@fdsTextInput.textInput path="form.annualProductionMonthForms[${monthForm_index}].oilMaxValue.inputValue" labelText="Oil max value" labelClass="govuk-visually-hidden"/>
              </td>
              <td class="govuk-table__cell">
                  <@fdsTextInput.textInput path="form.annualProductionMonthForms[${monthForm_index}].gasMinValue.inputValue" labelText="Gas min value" labelClass="govuk-visually-hidden"/>
              </td>
              <td class="govuk-table__cell">
                  <@fdsTextInput.textInput path="form.annualProductionMonthForms[${monthForm_index}].gasMaxValue.inputValue" labelText="Gas max value" labelClass="govuk-visually-hidden"/>
              </td>
            </tr>
          </#list>
        </tbody>
      </table>
      <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
      />
    </@fdsForm.htmlForm>
</@defaultPage>