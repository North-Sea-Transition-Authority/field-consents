<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestView" -->

<#macro applicationUpdateRequestSummary applicationUpdateRequestView>
  <@fdsSummaryList.summaryListCard
    headingText="Update request"
    summaryListId="application-update-request-summary-card">
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested by">
      ${applicationUpdateRequestView.requestedByUser()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested on">
      ${applicationUpdateRequestView.requestedByDateTime()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Request details">
      <@multiLineText.multiLineText contentText=applicationUpdateRequestView.requestText()!""/>
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Deadline">
      ${applicationUpdateRequestView.deadlineDate()!""}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>
