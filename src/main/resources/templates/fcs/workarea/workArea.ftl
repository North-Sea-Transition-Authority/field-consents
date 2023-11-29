<#include '../layout/layout.ftl'>
<#import '../dataitems/applicationDataItem.ftl' as applicationDataItem>
<#import '../dataitems/_dataItemFilter.ftl' as dataItemFilter>

<#-- @ftlvariable name="applicationDataItem" type="uk.co.nstauthority.fieldconsents.query.ApplicationDataItem" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_PAGE_WIDTH
>
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter oneThirdWidth=true>
      <@fdsSearch.searchFilterList clearFilterUrl=springUrl(clearFiltersUrl) filterButtonClass="govuk-button govuk-button--secondary">
        <@dataItemFilter.referenceNumberFilter form=form/>
        <@dataItemFilter.statusFilter form=form statusCheckboxes=appStatuses/>
        <@dataItemFilter.applicationTypeFilter form=form applicationTypeCheckboxes=appTypes/>
        <@dataItemFilter.durationFilter form=form durationCheckboxes=durationTypes/>
        <@assetFilter form=form prefilledAsset=prefilledAsset assetSearchRestUrl=assetSearchRestUrl/>
        <@dataItemFilter.operatorFilter form=form prefilledOperator=prefilledOperator operatorSearchRestUrl=operatorSearchRestUrl/>
        <@dataItemFilter.submittedYearFilter form=form/>
        <@geographicAreaFilter form=form geographicAreaCheckboxes=geographicAreas/>
        <@dataItemFilter.assetTypeWithShoreFilter form=form assetTypeWithShoreCheckboxes=assetTypesWithShore/>
        <#if caseOfficersAssigned?has_content>
          <@assignedCaseOfficerFilter form=form caseOfficerOptions=caseOfficersAssigned/>
        </#if>
        <#if technicalReviewersAssigned?has_content>
          <@assignedTechnicalReviewerFilter form=form technicalReviewerOptions=technicalReviewersAssigned/>
        </#if>
        <@dataItemFilter.licenceReferenceFilter form=form/>
      </@fdsSearch.searchFilterList>
    </@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent twoThirdsWidth=true>
      <#if isWorkAreaWithTabs>
        <@fdsBackendTabs.tabs tabsHeading="work-area tabs">
          <@fdsBackendTabs.tabList>
            <#list workAreaTabs as tab>
              <@fdsBackendTabs.tab tabLabel=tab.label tabUrl=tab.url tabAnchor=tab.anchor currentTab=selectedTab tabValue=tab.value />
            </#list>
          </@fdsBackendTabs.tabList>
          <#list workAreaTabs as tab>
            <@fdsBackendTabs.tabContent tabAnchor=tab.anchor currentTab=selectedTab tabValue=tab.value>
              <@fdsResultList.resultList resultCount=workAreaItems?size>
                <#list workAreaItems as workAreaItem>
                  <@applicationDataItem.applicationResultListItem dataItem=workAreaItem/>
                </#list>
              </@fdsResultList.resultList>
            </@fdsBackendTabs.tabContent>
          </#list>
        </@fdsBackendTabs.tabs>
      <#else>
        <@fdsResultList.resultList resultCount=workAreaItems?size>
          <#list workAreaItems as workAreaItem>
            <@applicationDataItem.applicationResultListItem dataItem=workAreaItem/>
          </#list>
        </@fdsResultList.resultList>
      </#if>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>

<#macro assetFilter form prefilledAsset assetSearchRestUrl>
  <@fdsSearch.searchFilterItem itemName="Primary field / facility" expanded=prefilledAsset.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path="form.assetKey"
      restUrl=springUrl(assetSearchRestUrl)
      labelText=""
      preselectedItems={prefilledAsset.id() : prefilledAsset.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro geographicAreaFilter form geographicAreaCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Geographic area" expanded=form.geographicAreas?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.geographicAreas"
      checkboxes=geographicAreaCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro assignedTechnicalReviewerFilter form technicalReviewerOptions>
  <@fdsSearch.searchFilterItem itemName="Technical reviewer assigned" expanded=form.technicalReviewerWuaId?has_content>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.technicalReviewerWuaId"
      options=technicalReviewerOptions
      labelText=""
      labelHeadingClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro assignedCaseOfficerFilter form caseOfficerOptions>
  <@fdsSearch.searchFilterItem itemName="Case officer assigned" expanded=form.caseOfficerWuaId?has_content>
    <@fdsSearchSelector.searchSelectorEnhanced
      path="form.caseOfficerWuaId"
      options=caseOfficerOptions
      labelText=""
      labelHeadingClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>
