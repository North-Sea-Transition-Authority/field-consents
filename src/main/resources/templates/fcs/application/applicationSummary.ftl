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
<#if !isSubmittable>
  <#assign warningBanner>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information">
      <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be submitted">
        Not all mandatory sections shown on the task list have been completed.
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#assign>
</#if>
<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
pageSize=getPageSize()
notificationBannerContent=warningBanner
>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
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
    <#if isSubmittable>
      <@fdsAction.submitButtons
      primaryButtonText="Submit"
      secondaryLinkText="Back to task list"
      linkSecondaryAction=true
      linkSecondaryActionUrl="${springUrl(cancelUrl)}"/>
    <#else>
      <@fdsAction.link linkText="Back to task list" linkUrl="${springUrl(cancelUrl)}"/>
    </#if>
  </@fdsForm.htmlForm>
</@defaultPage>