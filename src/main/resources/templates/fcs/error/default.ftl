<#include '../layout/layout.ftl'>
<#import './serviceSupport.ftl' as serviceSupportMacro>
<#import '../../fcs/layout/_pageSizes.ftl' as PageSize>

<#-- @ftlvariable name="stackTrace" type="String" -->
<#-- @ftlvariable name="errorRef" type="String" -->

<#assign pageTitle = "Sorry, there is a problem with the service" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  phaseBanner=false
  pageSize=stackTrace?has_content?then(PageSize.FULL_WIDTH, PageSize.TWO_THIRDS_COLUMN)>
  <p class="govuk-body">
    If you continue to experience this problem, contact the service desk using the
    details below. Be sure to include the error reference below in any correspondence.
  </p>
  <@_errorReference reference=errorRef>
    <@serviceSupportMacro.contactDetails emailSubject="Error reference - ${errorRef}" includeHeading=false />
  </@_errorReference>
</@defaultPage>

<#macro _errorReference reference>
  <#if reference?has_content>
    <div class="govuk-body">
      <p>Error reference: <span class="govuk-!-font-weight-bold">${reference}</span></p>
      <#nested/>
    </div>
    <#if stackTrace?has_content>
      <h2 class="govuk-heading-l">Stacktrace</h2>
      <@grid.gridRow>
        <@grid.fullColumn>
          <div style="overflow: scroll">
            <pre style="margin: 0; display: inline">
              <code>${stackTrace}</code>
            </pre>
          </div>
        </@grid.fullColumn>
      </@grid.gridRow>
    </#if>
  </#if>
</#macro>
