<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle="Flare report period"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <p class="govuk-body">
    We need to collect data relating to the flaring that has occurred over the most recent 12-month period.<br/>
    <br/>
    If the field, hub or facility has started up in this period, then enter zeros for the prior months with a relevant comment.
  </p>
  <@fdsForm.htmlForm>
    <@fdsRadio.radio
    path="form.reportEndYear.inputValue"
    radioItems=reportEndYearsMap
    labelText="Which year do you have flare report data up to?"/>
    <@fdsSelect.select
    path="form.reportEndMonth.inputValue"
    options=reportEndMonthsMap
    labelText="Which is the latest full month of flare report data you have?"/>
    <@fdsAction.submitButtons
    primaryButtonText="Save and continue"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
