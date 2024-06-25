<#include '../layout/layout.ftl'>
<#import '_ventInfo.ftl' as ventInfo>
<#import '../hints/copyPasteTableHint.ftl' as copyPasteTableHint>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList pageSize=PageSize.FULL_WIDTH>
    <@grid.gridRow>
      <@grid.twoThirdsColumn>
        <p class="govuk-body">
          Enter data relating to the venting that has occurred over the most recent 12-month period.<br/>
          <br/>
          If the field, hub or facility has started up in this period, then enter zeros for the prior months with a relevant comment.<br/>
          <br/>
          You can <@fdsAction.link linkText="adjust the reporting period" linkUrl=springUrl(periodUrl)/> if needed.
        </p>
        <@ventInfo.ventCategoryInfo/>
        <@copyPasteTableHint.hint/>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>
    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
      <table class="govuk-table" data-module="fcs-table-with-pastable-content">
        <tbody class="govuk-table__body">
        <tr class="govuk-table__row">
          <th class="govuk-table__header">Month</th>
          <th class="govuk-table__header">Category A (${categoryUnit})</th>
          <th class="govuk-table__header">Category B (${categoryUnit})</th>
          <th class="govuk-table__header">Category C (${categoryUnit})</th>
          <th class="govuk-table__header">Days of total shutdown</th>
          <th class="govuk-table__header govuk-!-width-one-third">Comments</th>
        </tr>
        <#list form.ventReportMonthForms as monthForm>
          <tr class="govuk-table__row">
            <#assign currentMonthForm = "form.ventReportMonthForms[${monthForm_index}]"/>
            <@spring.bind "${currentMonthForm}.month"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
            <@spring.bind "${currentMonthForm}.year"/>
            <input type="hidden" name="${spring.status.expression}" value="${spring.stringStatusValue}">
            <td class="govuk-table__cell">${monthForm.month[0..2]} ${monthForm.year}</td>
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
              <@fdsTextInput.textInput
                path="${currentMonthForm}.shutDownDays.inputValue"
                labelText="${monthForm.shutDownDays.displayName}"
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
