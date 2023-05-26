<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=getPageSize.getPageSize(wideSummaryDisplay)
backLinkUrl=springUrl(backLinkUrl)
>
  <#if caseProcessingActions?has_content>
    <@fdsAction.buttonGroup>
      <#list caseProcessingActions as action>
        <#assign actionClass>
          <#if action.primaryAction>
            govuk-button
          <#else>
            govuk-button govuk-button--secondary
          </#if>
        </#assign>
        <#if action.postUrl?has_content>
          <@fdsForm.htmlForm actionUrl=springUrl(action.postUrl)>
            <@fdsAction.button
              buttonText=action.displayName
              buttonClass=actionClass/>
          </@fdsForm.htmlForm>
        <#elseif action.redirectUrl?has_content>
          <@fdsAction.link
            linkUrl=springUrl(action.redirectUrl)
            linkText=action.displayName
            linkClass=actionClass/>
        </#if>
      </#list>
    </@fdsAction.buttonGroup>
  </#if>
  <@applicationSummary.applicationSummary accordionId=accordionId/>
</@defaultPage>
