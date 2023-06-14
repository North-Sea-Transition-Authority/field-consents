<#include '../layout/layout.ftl'>

<#macro caseActions actions>
  <#if actions?has_content>
    <@fdsAction.buttonGroup>
      <#list actions as action>
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
</#macro>
