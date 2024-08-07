<#include '../../../layout/layout.ftl'>
<#import '_consentBreachSummary.ftl' as consentBreachSummary>
<#import '../../_caseProcessingActions.ftl' as caseProcessingActions>

<#-- @ftlvariable name="consentBreachView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.breaches.ConsentBreachView" -->
<#-- @ftlvariable name="successfulDeleteBanner" type="String" -->

<#assign pageTitle = "Breach information"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=captionTitle
  backLinkUrl=springUrl(backLinkUrl)>

  <@caseProcessingActions.caseActions actions=breachInformationGroupActions/>
  <#if consentBreachView?has_content>
    <@consentBreachSummary.consentBreachSummary
      consentBreachView=consentBreachView
      consentBreachSummaryCardActions=breachInformationCardGroupActions/>
  <#else>
    <@fdsInsetText.insetText>
      Consent not breached.
    </@fdsInsetText.insetText>
  </#if>
</@defaultPage>
