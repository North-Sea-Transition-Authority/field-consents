<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  backLinkUrl=springUrl(backLinkUrl)
>

<@fdsForm.htmlForm actionUrl=springUrl(deleteUrl)>
  <@fdsDetails.summaryDetails summaryTitle="View the draft application to be deleted">
    <@applicationSummary.applicationSummary accordionId=accordionId/>
  </@fdsDetails.summaryDetails>
  <@fdsAction.submitButtons
    primaryButtonText="Delete draft application"
    secondaryLinkText="Back to task list"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(backLinkUrl)
    primaryButtonClass="govuk-button govuk-button--warning"/>
</@fdsForm.htmlForm>
</@defaultPage>
