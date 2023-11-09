<#include '../layout/layout.ftl'>
<#import '_ventSummary.ftl' as ventSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="ventView" type="uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentView" -->

<#assign pageTitle = "Are you sure you want to delete this vent system?"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>

  <@ventSummary.ventSummary vent=ventView showActions=false/>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <@fdsAction.submitButtons
    primaryButtonText="Delete"
    primaryButtonClass="govuk-button govuk-button--warning"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
