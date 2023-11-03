<#include '../../layout/layout.ftl'>
<#import '_applicationUpdateRequestSummary.ftl' as applicationUpdateRequestSummary>

<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestView" -->

<#macro applicationUpdateRequestHiddenSummary applicationUpdateRequestView>
  <#if applicationUpdateRequestView?has_content>
    <@fdsDetails.summaryDetails summaryTitle="What information have I been asked to update?">
      <@applicationUpdateRequestSummary.applicationUpdateRequestSummary applicationUpdateRequestView=applicationUpdateRequestView/>
    </@fdsDetails.summaryDetails>
  </#if>
</#macro>
