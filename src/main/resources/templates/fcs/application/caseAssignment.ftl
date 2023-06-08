<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.caseOfficerWuaId"
      options=caseOfficerAssignmentCandidates
      labelText="Select a case officer"/>
    <@fdsAction.submitButtons
      linkSecondaryAction=true
      secondaryLinkText="Cancel"
      primaryButtonText="Assign case officer"
      linkSecondaryActionUrl="${springUrl(backLinkUrl)}"/>
  </@fdsForm.htmlForm>
</@defaultPage>
