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
      <@dataItemFilter.operatorFilter form=form prefilledOperator=prefilledOperator operatorSearchRestUrl=operatorSearchRestUrl/>
      <@submittedYearFilter form=form/>
      <#if aceStatuses?has_content>
        <@aceFilter form=form aceCheckboxes=aceStatuses/>
      </#if>
      <@dataItemFilter.assetTypeWithShoreFilter form=form assetTypeWithShoreCheckboxes=assetTypesWithShore/>
      <@fieldAssetFilter form=form prefilledField=prefilledField fieldAssetSearchRestUrl=fieldAssetSearchRestUrl/>
      <@terminalAssetFilter form=form prefilledTerminal=prefilledTerminal terminalAssetSearchRestUrl=terminalAssetSearchRestUrl/>
    </@fdsSearch.searchFilterList>
  </@fdsSearch.searchFilter>
  <@fdsSearch.searchPageContent twoThirdsWidth=true>
    <#if searchInvoked>
      <#if searchResultItems?has_content>
        <@fdsResultList.resultList resultCount=searchResultItems?size>
          <#list searchResultItems as searchResultItem>
            <@fcsApplicationDataItem dataItem=searchResultItem pageTitle=pageTitle/>
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

<#macro fcsApplicationDataItem dataItem pageTitle>
  <#assign searchItemTagContent>
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
    itemTag=searchItemTagContent
  >
  <@fdsResultList.resultListDataItem>
    <#assign consentType>
      ${dataItem.getType()} <br/> ${dataItem.getDuration()} <br/> ${dataItem.getAceFlag()}
    </#assign>
    <#assign location>
      ${dataItem.getAsset()} <br/> ${dataItem.getGeographicArea()}
      <#if dataItem.getLicences()?has_content>
        <br/> ${dataItem.getLicences()}
      </#if>
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

<#macro submittedYearFilter form>
  <@fdsSearch.searchFilterItem itemName="Submission year" expanded=form.submittedYear?has_content>
    <@fdsSearch.searchTextInput
      path="form.submittedYear"
      labelText=""
      suffixScreenReaderPrompt="Application submission year"
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
