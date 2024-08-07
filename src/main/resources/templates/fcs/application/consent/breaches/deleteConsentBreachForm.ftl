<#include '../../../layout/layout.ftl'>
<#import '_consentBreachSummary.ftl' as consentBreachSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="assetView" type="uk.co.nstauthority.fieldconsents.application.assets.AssetView" -->

<#assign pageTitle = "Are you sure you want to unmark this consent as breached?"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>

    <@consentBreachSummary.consentBreachSummary
    consentBreachView=consentBreachView
    consentBreachSummaryCardActions=breachInformationCardGroupActions/>

    <@fdsForm.htmlForm>
        <@fdsAction.submitButtons
        primaryButtonText="Delete"
        primaryButtonClass="govuk-button govuk-button--warning"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(cancelUrl)
        />
    </@fdsForm.htmlForm>
</@defaultPage>
