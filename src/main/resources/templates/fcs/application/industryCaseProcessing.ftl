<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_caseProcessingActions.ftl' as caseProcessingActions>
<#import 'update/_applicationUpdateRequestBanner.ftl' as applicationUpdateRequestBanner>

<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestView" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)>
  <@applicationUpdateRequestBanner.applicationUpdateRequestBanner applicationUpdateRequestView=applicationUpdateRequestView!""/>
  <@caseProcessingActions.caseActions actions=actionList/>
  <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>
