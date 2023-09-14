<#include '../layout/layout.ftl'>
<#import '../dataitems/_dataItemFilter.ftl' as dataItemFilter>

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
        <@geographicAreaFilter form=form geographicAreaCheckboxes=geographicAreas/>
        <@dataItemFilter.assetTypeWithShoreFilter form=form assetTypeWithShoreCheckboxes=assetTypesWithShore/>
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
                  <@fcsApplicationDataItem dataItem=workAreaItem pageTitle=pageTitle/>
                </#list>
              </@fdsResultList.resultList>
            </@fdsBackendTabs.tabContent>
          </#list>
        </@fdsBackendTabs.tabs>
      <#else>
        <@fdsResultList.resultList resultCount=workAreaItems?size>
          <#list workAreaItems as workAreaItem>
            <@fcsApplicationDataItem dataItem=workAreaItem pageTitle=pageTitle/>
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
  <@fdsSearch.searchFilterItem itemName="Sea location" expanded=form.geographicAreas?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.geographicAreas"
      checkboxes=geographicAreaCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro fcsApplicationDataItem dataItem pageTitle>
  <#assign workAreaItemTagContent>
    <#if dataItem.isWithdrawalOpen()>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Withdrawal requested"/>
    </#if>
    <#if dataItem.isApplicationUpdateOpen()>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Update due by ${dataItem.getApplicationUpdateDeadline()}"/>
    </#if>
    <#if dataItem.isConsultationOpen()>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Consultation due by ${dataItem.getConsultationDeadline()}"/>
    </#if>
  </#assign>
  <@fdsResultList.resultListItem
    linkHeadingText=dataItem.getReference()
    linkHeadingUrl=springUrl(dataItem.url())
    captionHeadingText=dataItem.getOperator()
    itemTag=workAreaItemTagContent
  >
    <@fdsResultList.resultListDataItem>
      <#assign consentType>
        ${dataItem.getType()} <br/> ${dataItem.getDuration()} <br/> ${dataItem.getAceFlag()}
      </#assign>
      <#assign location>
        ${dataItem.getAsset()} <br/> ${dataItem.getGeographicArea()}
      </#assign>
      <#assign status>
        ${dataItem.getStatus()} <br/> ${dataItem.getCaseOfficer()} <br/> ${dataItem.getTechnicalReviewer()}
      </#assign>
      <#assign otherInformation>
        ${dataItem.getSubmittedDateTime()} <br/> ${dataItem.getSubmittedBy()}
      </#assign>
      <@fdsResultList.resultListDataValue key="Consent type" value=consentType/>
      <@fdsResultList.resultListDataValue key="Licence info" value=location/>
      <@fdsResultList.resultListDataValue key="Status" value=status/>
      <@fdsResultList.resultListDataValue key="Other information" value=otherInformation/>
    </@fdsResultList.resultListDataItem>
  </@fdsResultList.resultListItem>
</#macro>
