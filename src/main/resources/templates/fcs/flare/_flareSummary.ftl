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
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(flare.editUrl) itemText="Change" itemScreenReaderText="flare ${displayOrder}"/>
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(flare.deleteUrl) itemText="Delete" itemScreenReaderText="flare ${displayOrder}"/>
      </@fdsSummaryList.summaryListCardActionList>
    </#if>
  </#assign>
  <@fdsSummaryList.summaryListCard headingText="Flare ${displayOrder}" cardActionsContent=flareViewsActionsContent summaryListId="flare-view-summary-card-list">
    <@fdsSummaryList.summaryListRowNoAction keyText="Flare type">
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