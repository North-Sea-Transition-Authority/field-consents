<#include '../layout/layout.ftl'>
<#import '../summary/_simpleSummary.ftl' as simpleSummary>
<#import '../summary/_productionConsentSummary.ftl' as productionConsentSummary>
<#import '../summary/_emptySummary.ftl' as emptySummary>

<#-- @ftlvariable name="summarySections" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummarySection>" -->
<#-- @ftlvariable name="summaryItem" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummaryItem>" -->
<#-- @ftlvariable name="summaryGroup" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummaryGroup>" -->

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=PageSize.FULL_WIDTH
>
  <@fdsAccordion.accordion accordionId="summaryaccordian-${accordionId}">
    <#list summarySections as summarySection>
      <#list summarySection.summaryItems() as summaryItem>
        <@fdsAccordion.accordionSection sectionHeading=summaryItem.displayName()
          openSection=(summarySection?index == 0 && summaryItem?index == 0)>
          <#list summaryItem.summaryGroups() as summaryGroup>
            <#if summaryGroup.summaryGroupType() == "SIMPLE_SUMMARY">
              <@simpleSummary.simpleSummary
                summaryDataView=summaryGroup.summaryData()
                summaryHeading=summaryGroup.displayName()!""/>
            <#elseif summaryGroup.summaryGroupType() == "PRODUCTION_SHORT_TERM">
              <@productionConsentSummary.productionConsentSummary
                productionView=summaryGroup.summaryData()
                summaryHeading=summaryGroup.displayName()!""
                showConsentDays=true/>
            <#elseif summaryGroup.summaryGroupType() == "PRODUCTION_ANNUAL">
              <@productionConsentSummary.productionConsentSummary
                productionView=summaryGroup.summaryData()
                summaryHeading=summaryGroup.displayName()!""
                showConsentDays=false/>
            <#elseif summaryGroup.summaryGroupType() == "PRODUCTION_LONG_TERM">
              <@productionConsentSummary.productionConsentSummary
                productionView=summaryGroup.summaryData()
                summaryHeading=summaryGroup.displayName()!""
                showConsentDays=false/>
            <#elseif summaryGroup.summaryGroupType() == "EMPTY_SUMMARY">
              <@emptySummary.emptySummary/>
            </#if>
          </#list>
        </@fdsAccordion.accordionSection>
      </#list>
    </#list>
  </@fdsAccordion.accordion>
</@defaultPage>