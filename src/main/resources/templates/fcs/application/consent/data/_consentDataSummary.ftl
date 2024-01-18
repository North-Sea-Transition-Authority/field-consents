<#include '../../../layout/layout.ftl'>

<#-- @ftlvariable name="consentDataView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data.ConsentDataView" -->

<#macro summaryCard consentDataView editUrl>
  <#assign summaryCardActions>
    <@fdsSummaryList.summaryListCardActionItem
      itemUrl=springUrl(editUrl)
      itemText="Edit"
      itemScreenReaderText="Edit consent data"/>
  </#assign>
  <@fdsSummaryList.summaryListCard
    headingText="Consent data"
    headingSize="h3"
    summaryListId="summary-data-card-list"
    cardActionsContent=summaryCardActions>
    <@fdsSummaryList.summaryListRowNoAction keyText="Consent start date">
      ${consentDataView.consentStartDate()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Consent end date">
        ${consentDataView.consentEndDate()}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>
