<#include '../layout/layout.ftl'>
<#import '_summaryDetails.ftl' as summaryDetails>
<#import '../../fds/utilities/utilities.ftl' as fdsUtil>

<#-- @ftlvariable name="summarySections" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummarySection>" -->
<#-- @ftlvariable name="selectedApplicationVersionView" type="uk.co.nstauthority.fieldconsents.application.summary.ApplicationVersionView" -->
<#-- @ftlvariable name="applicationVersionViews" type="java.util.List<uk.co.nstauthority.fieldconsents.application.summary.ApplicationVersionView>" -->

<#macro applicationSummary accordionId selectedTab="">
  <#if applicationVersionViews?has_content && applicationVersionViews?size gt 1>
    <h2 class="govuk-heading-l">${selectedApplicationVersionView.displayText()}</h2>
    <@applicationVersions
      selectedApplicationVersionView=selectedApplicationVersionView
      applicationVersionViews=applicationVersionViews
      selectedTab=selectedTab
    />
  </#if>
  <@fdsAccordion.accordion accordionId="summaryaccordion-${accordionId}">
    <#list summarySections as summarySection>
      <#list summarySection.summaryItems() as summaryItem>
        <@fdsAccordion.accordionSection sectionHeading=summaryItem.displayName()
          openSection=(summarySection?index == 0 && summaryItem?index == 0)>
          <@summaryDetails.summaryDetails summaryItem=summaryItem/>
        </@fdsAccordion.accordionSection>
      </#list>
    </#list>
  </@fdsAccordion.accordion>
</#macro>

<#macro applicationVersions selectedApplicationVersionView applicationVersionViews selectedTab>
  <form method="GET" data-module="fds-html-form">
    <#if selectedTab?has_content>
      <input type="hidden" name="tab" value="${selectedTab.anchor}">
    </#if>
    <div class="inline-input-action">
      <#local id="version-number-selector">
      <div class="govuk-form-group">
        <label class="govuk-label" for="${id}">Select version</label>
        <select class="govuk-select" name="version" id="${id}" onchange="this.form.submit()">
          <#list applicationVersionViews as applicationVersionView>
            <#assign isSelected = applicationVersionView.id() == selectedApplicationVersionView.id()>
            <option value="${applicationVersionView.id()}" <#if isSelected>selected</#if>>
              ${applicationVersionView.displayText()}
            </option>
          </#list>
        </select>
      </div>
    </div>
  </form>
</#macro>
