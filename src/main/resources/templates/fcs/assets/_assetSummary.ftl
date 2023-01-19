<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="asset" type="uk.co.nstauthority.fieldconsents.assets.AssetView" -->

<#macro assetSummary
asset
showActions
displayOrder=""
>
  <#assign assetViewsActionsContent>
    <#if showActions>
      <@fdsSummaryList.summaryListCardActionList>
        <@fdsSummaryList.summaryListCardActionItem itemUrl=springUrl(asset.deleteUrl()) itemText="Delete" itemScreenReaderText="field ${displayOrder}"/>
      </@fdsSummaryList.summaryListCardActionList>
    </#if>
  </#assign>
  <@fdsSummaryList.summaryListCard headingText="Field ${displayOrder}" cardActionsContent=assetViewsActionsContent summaryListId="asset-view-summary-card-list">
    <@fdsSummaryList.summaryListRowNoAction keyText="Field">
      ${asset.assetName()}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Field operator">
      ${asset.assetOperatorName()}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>