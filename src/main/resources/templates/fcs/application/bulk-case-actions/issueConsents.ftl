<#include '../../layout/layout.ftl'>
<#import '_selectedApplications.ftl' as selectedApplicationsFtl>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <#list applicationDataItemViews as applicationDataItemView>
      <input type="hidden" name="selectedApplicationIds" value="${applicationDataItemView.applicationId()}">
    </#list>
    <@fdsAction.button buttonText="Issue consents"/>
  </@fdsForm.htmlForm>
  <@selectedApplicationsFtl.summaryList
    applicationDataItemViews=applicationDataItemViews
    captionHeadingFunction=captionHeadingFunction/>
</@defaultPage>