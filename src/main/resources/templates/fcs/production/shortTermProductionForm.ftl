<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Short Term Oil and Gas Production Request ${requestYear}"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
    <p class="govuk-body-l">
      Enter the amount of production expected for each month for the requested consent days only. For example, if the consent is due to end halfway through the month, only provide production volumes for half the month.
    </p>
    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
      <table class="govuk-table">
        <caption class="govuk-table__caption govuk-table__caption--m">${startDate} to ${endDate}</caption>
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header govuk-!-width-one-third">Month</th>
          <th class="govuk-table__header govuk-!-width-one-third">Consent Days</th>
          <th class="govuk-table__header govuk-!-width-one-third">Minimum Oil (${oilUnit})</th>
          <th class="govuk-table__header govuk-!-width-one-third">Maximum Oil (${oilUnit})</th>
          <th class="govuk-table__header govuk-!-width-one-third">Minimum Gas (${gasUnit})</th>
          <th class="govuk-table__header govuk-!-width-one-third">Maximum Gas (${gasUnit})</th>
        </tr>
        <#list form.shortTermProductionMonthForms as monthForm>
          <tr class="govuk-table__row">
              <@spring.bind "form.shortTermProductionMonthForms[${monthForm_index}].month"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <@spring.bind "form.shortTermProductionMonthForms[${monthForm_index}].year"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
            <@spring.bind "form.shortTermProductionMonthForms[${monthForm_index}].consentDays"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
            <@spring.bind "form.shortTermProductionMonthForms[${monthForm_index}].startDate"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
              <@spring.bind "form.shortTermProductionMonthForms[${monthForm_index}].endDate"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
            <td class="govuk-table__cell">${monthForm.month[0..2]} ${monthForm.year}</td>
            <td class="govuk-table__cell">${monthForm.consentDays}</td>
            <td class="govuk-table__cell">
                <@fdsTextInput.textInput path="form.shortTermProductionMonthForms[${monthForm_index}].oilMinValue.inputValue" labelText="Oil min value" labelClass="govuk-visually-hidden"/>
            </td>
            <td class="govuk-table__cell">
                <@fdsTextInput.textInput path="form.shortTermProductionMonthForms[${monthForm_index}].oilMaxValue.inputValue" labelText="Oil max value" labelClass="govuk-visually-hidden"/>
            </td>
            <td class="govuk-table__cell">
                <@fdsTextInput.textInput path="form.shortTermProductionMonthForms[${monthForm_index}].gasMinValue.inputValue" labelText="Gas min value" labelClass="govuk-visually-hidden"/>
            </td>
            <td class="govuk-table__cell">
                <@fdsTextInput.textInput path="form.shortTermProductionMonthForms[${monthForm_index}].gasMaxValue.inputValue" labelText="Gas max value" labelClass="govuk-visually-hidden"/>
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