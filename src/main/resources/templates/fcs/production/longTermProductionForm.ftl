<#include '../layout/layout.ftl'>
<#import '../hints/copyPasteTableHint.ftl' as copyPasteTableHint>

<#-- @ftlvariable name="startYear" type="String" -->
<#-- @ftlvariable name="endYear" type="String" -->
<#-- @ftlvariable name="oilUnit" type="String" -->
<#-- @ftlvariable name="gasUnit" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Long Term Oil and Gas Production Request"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <@copyPasteTableHint.hint/>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <table class="govuk-table" data-module="fcs-table-with-pastable-content">
      <caption class="govuk-table__caption govuk-table__caption--m">${startYear} to ${endYear}</caption>
      <tbody class="govuk-table__body">
      <tr class="govuk-table__row">
        <th class="govuk-table__header govuk-!-width-one-third">Year</th>
        <th class="govuk-table__header govuk-!-width-one-third">Minimum Oil (${oilUnit})</th>
        <th class="govuk-table__header govuk-!-width-one-third">Maximum Oil (${oilUnit})</th>
        <th class="govuk-table__header govuk-!-width-one-third">Minimum Gas (${gasUnit})</th>
        <th class="govuk-table__header govuk-!-width-one-third">Maximum Gas (${gasUnit})</th>
      </tr>
      <#list form.longTermProductionYearForms as yearForm>
        <tr class="govuk-table__row">
          <#assign currentYearForm = "form.longTermProductionYearForms[${yearForm_index}]"/>
          <@spring.bind "${currentYearForm}.year"/>
          <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
          <td class="govuk-table__cell">${yearForm.year}</td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput path="${currentYearForm}.oilMinValue.inputValue" labelText="Oil min value ${yearForm.year}" labelClass="govuk-visually-hidden"/>
          </td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput path="${currentYearForm}.oilMaxValue.inputValue" labelText="Oil max value ${yearForm.year}" labelClass="govuk-visually-hidden"/>
          </td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput path="${currentYearForm}.gasMinValue.inputValue" labelText="Gas min value ${yearForm.year}" labelClass="govuk-visually-hidden"/>
          </td>
          <td class="govuk-table__cell">
            <@fdsTextInput.textInput path="${currentYearForm}.gasMaxValue.inputValue" labelText="Gas max value ${yearForm.year}" labelClass="govuk-visually-hidden"/>
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
