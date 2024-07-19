<#include '../layout/layout.ftl'>
<#import '_summaryDetails.ftl' as summaryDetails>
<#import '../../fds/utilities/utilities.ftl' as fdsUtil>

<#-- @ftlvariable name="summarySections" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummarySection>" -->
<#-- @ftlvariable name="availableVersions" type="java.util.Map<String, String>" -->
<#-- @ftlvariable name="currentVersionNumber" type="String" -->

<#macro applicationSummary accordionId selectedTab="">
  <#if availableVersions?has_content>
    <h2 class="govuk-heading-l">Version ${currentVersionNumber}</h2>
    <@applicationVersions availableVersions=availableVersions currentVersionNumber=currentVersionNumber selectedTab=selectedTab/>
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

<#macro applicationVersions availableVersions currentVersionNumber selectedTab>
  <form method="GET" data-module="fds-html-form">
    <#if selectedTab?has_content>
      <input type="hidden" name="tab" value="${selectedTab.anchor}">
    </#if>
    <div class="inline-input-action">
      <#local id="version-number-selector">
      <div class="govuk-form-group">
        <label class="govuk-label" for="${id}">Select version</label>
        <select class="govuk-select" name="versionNumber" id="${id}" onchange="this.form.submit()">
          <#list availableVersions as key,value>
            <#assign isSelected = currentVersionNumber == key>
            <option value="${key}" <#if isSelected>selected</#if>>${value}</option>
          </#list>
        </select>
      </div>
    </div>
  </form>
</#macro>
