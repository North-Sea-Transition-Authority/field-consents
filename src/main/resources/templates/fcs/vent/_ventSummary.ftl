<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="vent" type="uk.co.nstauthority.fieldconsents.flarevent.vent.VentView" -->

<#macro ventSummary
  vent
  showActions
  displayOrder=""
>
  <#assign ventViewsActionsContent>
    <#if showActions>
      <@fdsSummaryList.summaryListCardActionList>
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(vent.editUrl) itemText="Change" itemScreenReaderText="vent ${displayOrder}"/>
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(vent.deleteUrl) itemText="Delete" itemScreenReaderText="vent ${displayOrder}"/>
      </@fdsSummaryList.summaryListCardActionList>
    </#if>
  </#assign>
  <@fdsSummaryList.summaryListCard headingText="Vent ${displayOrder}" cardActionsContent=ventViewsActionsContent summaryListId="vent-view-summary-card-list">
    <@fdsSummaryList.summaryListRowNoAction keyText="Vent type">
      ${vent.ventType}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Description">
      ${vent.description}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Metered">
      ${vent.meteredFlag}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Comments">
      ${vent.comments}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>