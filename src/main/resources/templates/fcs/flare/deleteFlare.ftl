<#include '../layout/layout.ftl'>
<#import './_flareSummary.ftl' as flareSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="flareView" type="uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareView" -->

<#assign pageTitle = "Are you sure you want to delete this flare?"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>

  <@flareSummary.flareSummary flare=flareView showActions=false/>

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