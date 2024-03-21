<#include '../../../layout/layout.ftl'>
<#import '../../_caseProcessingActions.ftl' as caseProcessingActions>

<#macro summary heading fileViews caseProcessingActionViewList>
  <#assign summaryCardActions>
    <@caseProcessingActions.summaryCardActions actionViews=caseProcessingActionViewList />
  </#assign>
  <@fdsSummaryList.summaryListCard
    headingText=heading
    headingSize="h3"
    summaryListId="files-summary-card-list"
    cardActionsContent=summaryCardActions>
    <#list fileViews as fileView>
      <@fdsSummaryList.summaryListRow
        keyText=fileView.filename()
        actionText="Download"
        actionUrl=springUrl(fileView.downloadUrl())
        screenReaderActionText="Download ${fileView.filename()}">
          <p class="govuk-body">
            <@multiLineText.multiLineText contentText=fileView.description()/>
          </p>
      </@fdsSummaryList.summaryListRow>
    </#list>
  </@fdsSummaryList.summaryListCard>
</#macro>
