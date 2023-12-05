<#include '../../layout/layout.ftl'>
<#import '_selectedApplications.ftl' as selectedApplicationsFtl>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <#list applicationDataItems as applicationDataItem>
      <input type="hidden" name="selectedApplicationIds" value="${applicationDataItem.applicationId()}">
    </#list>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.caseOfficerWuaId"
      options=caseOfficerOptions
      labelText="Select a case officer"
      />
    <@fdsAction.button buttonText="Assign case officer"/>
  </@fdsForm.htmlForm>
  <@selectedApplicationsFtl.summaryList
    applicationDataItems=applicationDataItems
    captionHeadingFunction=captionHeadingFunction
    />
</@defaultPage>
