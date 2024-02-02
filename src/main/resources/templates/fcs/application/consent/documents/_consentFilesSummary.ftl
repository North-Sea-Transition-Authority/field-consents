<#include '../../../layout/layout.ftl'>

<#macro summary heading fileViews editUrl>
  <#assign summaryCardActions>
    <@fdsSummaryList.summaryListCardActionItem
      itemUrl=springUrl(editUrl)
      itemText="Edit"
      itemScreenReaderText="Edit consent documents"/>
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
