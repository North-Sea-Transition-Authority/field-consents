<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsWarning.warning>
    Once the application is closed, no further processing is allowed. This action cannot be undone.
  </@fdsWarning.warning>

  <@fdsDetails.summaryDetails summaryTitle="View the application to be closed">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
  </@fdsDetails.summaryDetails>

  <@fdsForm.htmlForm actionUrl=springUrl(closureUrl)>
    <@fdsAction.submitButtons
      primaryButtonText="Close application"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)
      primaryButtonClass="govuk-button govuk-button--warning"/>
  </@fdsForm.htmlForm>
</@defaultPage>
