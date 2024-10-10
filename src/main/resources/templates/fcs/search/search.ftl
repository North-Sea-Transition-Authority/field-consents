<#include '../layout/layout.ftl'>
<#import '../dataitems/applicationDataItem.ftl' as applicationDataItem>
<#import '../dataitems/_dataItemFilter.ftl' as dataItemFilter>
<#import './_noResultsFound.ftl' as noResultsFound>

<#-- @ftlvariable name="searchResultItem" type="uk.co.nstauthority.fieldconsents.query.ApplicationDataItemView" -->

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
      <@dataItemFilter.operatorFilter form=form prefilledOperator=prefilledOperator operatorSearchRestUrl=operatorSearchRestUrl/>
      <#if canFilterByPrimaryOperatorGroup>
        <@dataItemFilter.operatorGroupFilter form=form prefilledOperatorGroup=prefilledOperatorGroup operatorGroupSearchRestUrl=operatorGroupSearchRestUrl/>
      </#if>
      <@dataItemFilter.submittedYearFilter form=form/>
      <@consentStartYearFilter form=form/>
      <@consentEndYearFilter form=form/>
      <#if approvedForIssue?has_content>
        <@dataItemFilter.approvedForIssueFilter form=form expanded=form.approvedForIssue?has_content/>
      </#if>
      <#if aceStatuses?has_content>
        <@dataItemFilter.aceFilter form=form expanded=form.aceFlagStatuses?has_content aceCheckboxes=aceStatuses/>
      </#if>
      <@dataItemFilter.assetTypeWithShoreFilter form=form assetTypeWithShoreCheckboxes=assetTypesWithShore expanded=form.assetTypesWithShore?has_content/>
      <@dataItemFilter.fieldAssetFilter form=form prefilledField=prefilledField fieldAssetSearchRestUrl=fieldAssetSearchRestUrl/>
      <@dataItemFilter.terminalAssetFilter form=form prefilledTerminal=prefilledTerminal terminalAssetSearchRestUrl=terminalAssetSearchRestUrl/>
      <@dataItemFilter.licenceReferenceFilter form=form/>
    </@fdsSearch.searchFilterList>
  </@fdsSearch.searchFilter>
  <@fdsSearch.searchPageContent twoThirdsWidth=true>
    <#if searchInvoked>
      <#if searchResultItems?has_content>
        <@fdsResultList.resultList resultCount=searchResultItems?size>
          <#list searchResultItems as searchResultItem>
            <@applicationDataItem.applicationResultListItem dataItem=searchResultItem/>
          </#list>
        </@fdsResultList.resultList>
        <#if searchResultsLimited>
          <@fdsInsetText.insetText>
            Only showing first ${searchResultItems?size} results.
          </@fdsInsetText.insetText>
        </#if>
      <#else>
        <@noResultsFound.noResultsFoundMessageWithHints>
          <li>clearing filters</li>
          <li>double-checking your reference number</li>
          <li>searching for something less specific</li>
        </@noResultsFound.noResultsFoundMessageWithHints>
      </#if>
    </#if>
  </@fdsSearch.searchPageContent>
</@fdsSearch.searchPage>
</@defaultPage>

<#macro consentStartYearFilter form>
  <@fdsSearch.searchFilterItem itemName="Consent start year" expanded=form.consentStartYear?has_content>
    <@fdsSearch.searchTextInput
      path="form.consentStartYear"
      labelText=""
      suffixScreenReaderPrompt="Consent start year"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro consentEndYearFilter form>
  <@fdsSearch.searchFilterItem itemName="Consent end year" expanded=form.consentEndYear?has_content>
    <@fdsSearch.searchTextInput
      path="form.consentEndYear"
      labelText=""
      suffixScreenReaderPrompt="Consent end year"
    />
  </@fdsSearch.searchFilterItem>
</#macro>
