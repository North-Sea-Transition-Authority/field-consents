<#include '../layout/layout.ftl'>
<#import '../summary/_simpleSummary.ftl' as simpleSummary>
<#import '../summary/_tableSummary.ftl' as tableSummary>
<#import '../summary/_emptySummary.ftl' as emptySummary>

<#-- @ftlvariable name="summarySections" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummarySection>" -->
<#-- @ftlvariable name="summaryItem" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummaryItem>" -->
<#-- @ftlvariable name="summaryCard" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummaryCard>" -->

<#function getPageSize>
  <#if wideSummaryDisplay>
    <#return PageSize.FULL_PAGE_WIDTH/>
  <#else>
    <#return PageSize.FULL_WIDTH/>
  </#if>
</#function>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=getPageSize()
>
  <@fdsAccordion.accordion accordionId="summaryaccordian-${accordionId}">
    <#list summarySections as summarySection>
      <#list summarySection.summaryItems() as summaryItem>
        <@fdsAccordion.accordionSection sectionHeading=summaryItem.displayName()
          openSection=(summarySection?index == 0 && summaryItem?index == 0)>
          <#list summaryItem.summaryCards() as summaryCard>
            <#if summaryCard.summaryCardType() == "SIMPLE_SUMMARY">
              <@simpleSummary.simpleSummary
                summaryDataView=summaryCard.summaryData()
                summaryHeading=summaryCard.displayName()!""/>
            <#elseif summaryCard.summaryCardType() == "TABLE_SUMMARY">
              <@tableSummary.tableSummary
                summaryTableView=summaryCard.summaryData()
                summaryHeading=summaryCard.displayName()!""/>
            <#elseif summaryCard.summaryCardType() == "EMPTY_SUMMARY">
              <@emptySummary.emptySummary/>
            </#if>
          </#list>
        </@fdsAccordion.accordionSection>
      </#list>
    </#list>
  </@fdsAccordion.accordion>
</@defaultPage>