<#include '../../../layout/layout.ftl'>
<#import '../../_caseProcessingActions.ftl' as caseProcessingActions>

<#macro summary heading fileViews caseProcessingActionViewList=[]>
  <#assign summaryCardActions>
    <#if caseProcessingActionViewList?has_content>
      <@caseProcessingActions.summaryCardActions actionViews=caseProcessingActionViewList />
    </#if>
  </#assign>
  <@fdsSummaryList.summaryListCard
    headingText=heading
    headingSize="h2"
    summaryListId="files-summary-card-list"
    cardActionsContent=summaryCardActions>
    <#list fileViews as fileView>
      <@fdsSummaryList.summaryListRow
        keyText=fileView.filename()
        actionText="Download"
        actionUrl=springUrl(fileView.downloadUrl())
        screenReaderActionText="${fileView.filename()}">
          <p class="govuk-body">
            <@multiLineText.multiLineText contentText=fileView.description()/>
          </p>
      </@fdsSummaryList.summaryListRow>
    </#list>
  </@fdsSummaryList.summaryListCard>
</#macro>
