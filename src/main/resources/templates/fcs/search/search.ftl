<#include '../layout/layout.ftl'>
<#import '../dataitems/applicationDataItem.ftl' as applicationDataItem>
<#import '../dataitems/_dataItemFilter.ftl' as dataItemFilter>

<#-- @ftlvariable name="searchResultItem" type="uk.co.nstauthority.fieldconsents.search.SearchResultItem" -->

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
      <@dataItemFilter.submittedYearFilter form=form/>
      <@consentStartYearFilter form=form/>
      <#if aceStatuses?has_content>
        <@aceFilter form=form aceCheckboxes=aceStatuses/>
      </#if>
      <@dataItemFilter.assetTypeWithShoreFilter form=form assetTypeWithShoreCheckboxes=assetTypesWithShore/>
      <@fieldAssetFilter form=form prefilledField=prefilledField fieldAssetSearchRestUrl=fieldAssetSearchRestUrl/>
      <@terminalAssetFilter form=form prefilledTerminal=prefilledTerminal terminalAssetSearchRestUrl=terminalAssetSearchRestUrl/>
      <@licenceReferenceFilter form=form/>
    </@fdsSearch.searchFilterList>
  </@fdsSearch.searchFilter>
  <@fdsSearch.searchPageContent twoThirdsWidth=true>
    <#if searchInvoked>
      <#if searchResultItems?has_content>
        <@fdsResultList.resultList resultCount=searchResultItems?size>
          <#list searchResultItems as searchResultItem>
            <@applicationDataItem.applicationResultListItem
              dataItem=searchResultItem.applicationDataItem()
              licenses=searchResultItem.licenses()!""/>
          </#list>
        </@fdsResultList.resultList>
      <#else>
        <h3 class="govuk-heading-s">There are no results matching your search</h3>
        <p class="govuk-body">Improve your search by:</p>
        <ul class="govuk-list govuk-list--bullet">
          <li>clearing filters</li>
          <li>double-checking your reference number</li>
          <li>searching for something less specific</li>
        </ul>
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

<#macro aceFilter form aceCheckboxes>
  <@fdsSearch.searchFilterItem itemName="ACE status" expanded=form.aceFlagStatuses?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.aceFlagStatuses"
      checkboxes=aceCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro fieldAssetFilter form prefilledField fieldAssetSearchRestUrl>
  <@fdsSearch.searchFilterItem itemName="Field" expanded=prefilledField.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path="form.fieldAssetKey"
      restUrl=springUrl(fieldAssetSearchRestUrl)
      labelText=""
      preselectedItems={prefilledField.id() : prefilledField.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro terminalAssetFilter form prefilledTerminal terminalAssetSearchRestUrl>
  <@fdsSearch.searchFilterItem itemName="Facility" expanded=prefilledTerminal.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path="form.terminalAssetKey"
      restUrl=springUrl(terminalAssetSearchRestUrl)
      labelText=""
      preselectedItems={prefilledTerminal.id() : prefilledTerminal.text()}
      inputClass="govuk-input--width-10"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro licenceReferenceFilter form>
  <@fdsSearch.searchFilterItem itemName="Licence reference" expanded=form.licenceReference?has_content>
    <@fdsSearch.searchTextInput
      path="form.licenceReference"
      labelText=""
      suffixScreenReaderPrompt="Licence reference"
    />
  </@fdsSearch.searchFilterItem>
</#macro>
