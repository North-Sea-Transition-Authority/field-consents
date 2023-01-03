<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle="Vent report period"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <p class="govuk-body-lead">
    We need to collect data relating to the venting that has occurred over the most recent 12-month period.<br/>
    <br/>
    If the field or hub has started-up during this period, then enter zeros for the prior months with a relevant comment.
  </p>
    <@fdsForm.htmlForm>
        <@fdsRadio.radio
        path="form.reportEndYear.inputValue"
        radioItems=reportEndYearsMap
        labelText="Which year do you have vent report data up to?"/>
        <@fdsSelect.select
        path="form.reportEndMonth.inputValue"
        options=reportEndMonthsMap
        labelText="Which is the latest full month of vent report data you have?"/>
        <@fdsAction.submitButtons
        primaryButtonText="Save and continue"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>
</@defaultPage>