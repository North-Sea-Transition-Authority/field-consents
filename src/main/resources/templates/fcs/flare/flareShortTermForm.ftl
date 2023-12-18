<#include '../layout/layout.ftl'>
<#import './_flareCategoryInfo.ftl' as flareCategoryInfo>
<#import '../hints/copyPasteTableHint.ftl' as copyPasteTableHint>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Short term consent">

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList pageSize=PageSize.FULL_WIDTH>
  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <p class="govuk-body-l">
        Enter the amount of flaring expected for each month for the requested consent days only. For example, if the consent is due to end halfway through the month, only provide flare volumes for half the month.
      </p>
      <@flareCategoryInfo.flareCategoryInfo/>
      <@copyPasteTableHint.hint/>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <table class="govuk-table" data-module="fcs-table-with-pastable-content">
      <caption class="govuk-table__caption govuk-table__caption--m">${startDate} to ${endDate}</caption>
      <tbody class="govuk-table__body">
      <tr class="govuk-table__row">
        <th class="govuk-table__header">Month</th>
        <th class="govuk-table__header">Consent Days</th>
        <th class="govuk-table__header">Category A (${categoryUnit})</th>
        <th class="govuk-table__header">Category B (${categoryUnit})</th>
        <th class="govuk-table__header">Category C (${categoryUnit})</th>
        <th class="govuk-table__header govuk-!-width-one-third">Comments</th>
      </tr>
      <#list form.flareShortTermMonthForms as monthForm>
        <tr class="govuk-table__row">
          <#assign currentMonthForm = "form.flareShortTermMonthForms[${monthForm_index}]"/>
          <@spring.bind "${currentMonthForm}.month"/>
          <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
          <@spring.bind "${currentMonthForm}.year"/>
          <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
          <@spring.bind "${currentMonthForm}.startDate"/>
          <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
          <@spring.bind "${currentMonthForm}.endDate"/>
          <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
          <@spring.bind "${currentMonthForm}.consentDays"/>
          <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
          <td class="govuk-table__cell">${monthForm.month[0..2]} ${monthForm.year}</td>
          <td class="govuk-table__cell">${monthForm.consentDays}</td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput
              path="${currentMonthForm}.categoryA.inputValue"
              labelText="${monthForm.categoryA.displayName}"
              formGroupClass="govuk-!-margin-bottom-0"
              labelClass="govuk-visually-hidden"/>
          </td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput
              path="${currentMonthForm}.categoryB.inputValue"
              labelText="${monthForm.categoryB.displayName}"
              formGroupClass="govuk-!-margin-bottom-0"
              labelClass="govuk-visually-hidden"/>
          </td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput
              path="${currentMonthForm}.categoryC.inputValue"
              labelText="${monthForm.categoryC.displayName}"
              formGroupClass="govuk-!-margin-bottom-0"
              labelClass="govuk-visually-hidden"/>
          </td>
          <td class="govuk-table__cell">
            <@fdsTextarea.textarea
              path="${currentMonthForm}.comments.inputValue"
              labelText="${monthForm.comments.displayName}"
              formGroupClass="govuk-!-margin-bottom-0"
              inputClass="govuk-!-margin-bottom-0"
              labelClass="govuk-visually-hidden"
              rows="2"/>
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
