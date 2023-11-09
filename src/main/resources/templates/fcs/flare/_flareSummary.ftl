<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="flare" type="uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareView" -->

<#macro flareSummary
  flare
  showActions
  displayOrder=""
>
  <#assign flareViewsActionsContent>
    <#if showActions>
      <@fdsSummaryList.summaryListCardActionList>
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(flare.editUrl) itemText="Change" itemScreenReaderText="flare system ${displayOrder}"/>
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(flare.deleteUrl) itemText="Delete" itemScreenReaderText="flare system ${displayOrder}"/>
      </@fdsSummaryList.summaryListCardActionList>
    </#if>
  </#assign>
  <@fdsSummaryList.summaryListCard headingText="Flare system ${displayOrder}" cardActionsContent=flareViewsActionsContent summaryListId="flare-system-view-summary-card-list">
    <@fdsSummaryList.summaryListRowNoAction keyText="Flare system type">
      ${flare.flareType}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Description">
      ${flare.description}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Metered">
      ${flare.meteredFlag}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Comments">
      ${flare.comments}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>
