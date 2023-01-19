<#include '../layout/layout.ftl'>
<#import '_assetSummary.ftl' as assetSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="assetView" type="uk.co.nstauthority.fieldconsents.assets.AssetView" -->

<#assign pageTitle = "Are you sure you want to delete this field?"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>

    <@assetSummary.assetSummary asset=assetView showActions=false/>

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