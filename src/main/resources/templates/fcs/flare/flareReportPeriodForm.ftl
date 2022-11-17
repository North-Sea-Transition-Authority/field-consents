<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle="Flare report period"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <p class="govuk-body">
    We need to collect data relating to the flaring that has occurred over the past 12 months.
  </p>
  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
    path="form.hasDataForPeriod"
    labelText="Do you have full months of flare report data for the period ${reportPeriodStart} to ${reportPeriodEnd}?"
    hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.hasDataForPeriod"/>
      <@fdsRadio.radioNo path="form.hasDataForPeriod">
        <@fdsRadio.radio
        path="form.reportEndYear.inputValue"
        nestingPath="form.hasDataForPeriod"
        radioItems=reportEndYearsMap
        labelText="Which year do you have flare report data up to?"/>
        <@fdsSelect.select
        path="form.reportEndMonth.inputValue"
        nestingPath="form.hasDataForPeriod"
        options=reportEndMonthsMap
        labelText="Which is the latest full month of flare report data you have?"/>
      </@fdsRadio.radioNo>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
    primaryButtonText="Save and continue"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>