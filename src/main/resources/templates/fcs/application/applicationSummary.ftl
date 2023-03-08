<#include '../layout/layout.ftl'>
<#import '../summary/_simpleSummary.ftl' as simpleSummary>
<#import '../summary/_productionConsentSummary.ftl' as productionConsentSummary>

<#-- @ftlvariable name="summarySections" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummarySection>" -->

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=PageSize.FULL_WIDTH
>
  <@fdsAccordion.accordion accordionId="summaryaccordian-${accordionId}">
    <#list summarySections as summarySection>
      <#list summarySection.summaryItems() as summaryItem>
        <@fdsAccordion.accordionSection sectionHeading=summaryItem.displayName() openSection=true>
          <#if summaryItem.summaryItemType() == "SIMPLE_SUMMARY">
            <@simpleSummary.simpleSummary
              summaryDataView=summaryItem.summaryData()
              summaryHeading=summaryItem.displayName()/>
          <#elseif summaryItem.summaryItemType() == "PRODUCTION_SHORT_TERM">
            <@productionConsentSummary.productionConsentSummary
              productionView=summaryItem.summaryData()
              summaryHeading=summaryItem.displayName()
              showConsentDays=true/>
          <#elseif summaryItem.summaryItemType() == "PRODUCTION_ANNUAL">
            <@productionConsentSummary.productionConsentSummary
              productionView=summaryItem.summaryData()
              summaryHeading=summaryItem.displayName()
              showConsentDays=false/>
          <#elseif summaryItem.summaryItemType() == "PRODUCTION_LONG_TERM">
            <@productionConsentSummary.productionConsentSummary
              productionView=summaryItem.summaryData()
              summaryHeading=summaryItem.displayName()
              showConsentDays=false/>
          </#if>
        </@fdsAccordion.accordionSection>
      </#list>
    </#list>
  </@fdsAccordion.accordion>
</@defaultPage>