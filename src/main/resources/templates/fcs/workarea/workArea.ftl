<#include '../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
>
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter oneThirdWidth=true>
      <@fdsSearch.searchFilterList clearFilterUrl=springUrl(clearFiltersUrl) filterButtonClass="govuk-button govuk-button--secondary">
        <@referenceNumberFilter form=form/>
        <@statusFilter form=form statusCheckboxes=appStatuses/>
        <@applicationTypeFilter form=form applicationTypeCheckboxes=appTypes/>
        <@durationFilter form=form durationCheckboxes=durationTypes/>
        <@assetFilter form=form prefilledAsset=prefilledAsset assetSearchRestUrl=assetSearchRestUrl/>
        <@operatorFilter form=form prefilledOperator=prefilledOperator operatorSearchRestUrl=operatorSearchRestUrl/>
        <@geographicAreaFilter form=form geographicAreaCheckboxes=geographicAreas/>
        <@assetTypeWithShoreFilter form=form assetTypeWithShoreCheckboxes=assetTypesWithShore/>
      </@fdsSearch.searchFilterList>
    </@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent twoThirdsWidth=true>
      <@fdsResultList.resultList resultCount=workAreaItems?size>
        <#list workAreaItems as workAreaItem>
          <@fcsWorkAreaItem workAreaItem=workAreaItem/>
        </#list>
      </@fdsResultList.resultList>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>

<#macro referenceNumberFilter form>
  <@fdsSearch.searchFilterItem itemName="Reference number" expanded=form.referenceSearchTerm?has_content>
    <@fdsSearch.searchTextInput
      path="form.referenceNumber"
      labelText=""
      suffixScreenReaderPrompt="Application reference number"
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro statusFilter form statusCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Status" expanded=form.statuses?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.statuses"
      checkboxes=statusCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro applicationTypeFilter form applicationTypeCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Application type" expanded=form.applicationTypes?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.applicationTypes"
      checkboxes=applicationTypeCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro durationFilter form durationCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Duration" expanded=form.durationTypes?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.durationTypes"
      checkboxes=durationCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

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

<#macro operatorFilter form prefilledOperator operatorSearchRestUrl>
  <@fdsSearch.searchFilterItem itemName="Primary operator" expanded=prefilledOperator.id()?has_content>
    <@fdsSearchSelector.searchSelectorRest
      path="form.operatorId"
      restUrl=springUrl(operatorSearchRestUrl)
      labelText=""
      preselectedItems={prefilledOperator.id() : prefilledOperator.text()}
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

<#macro assetTypeWithShoreFilter form assetTypeWithShoreCheckboxes>
  <@fdsSearch.searchFilterItem itemName="Asset type" expanded=form.assetTypesWithShore?has_content>
    <@fdsSearch.searchCheckboxes
      path="form.assetTypesWithShore"
      checkboxes=assetTypeWithShoreCheckboxes
    />
  </@fdsSearch.searchFilterItem>
</#macro>

<#macro fcsWorkAreaItem workAreaItem>
  <@fdsResultList.resultListItem
    linkHeadingText=workAreaItem.reference()
    linkHeadingUrl=springUrl(workAreaItem.url())
    captionHeadingText=workAreaItem.operator()
  >
    <@fdsResultList.resultListDataItem>
        <#assign consentType>
          ${workAreaItem.type()} <br/> ${workAreaItem.duration()} <br/> ${workAreaItem.aceFlag()}
        </#assign>
        <#assign location>
          ${workAreaItem.asset()} <br/> ${workAreaItem.geographicArea()}
        </#assign>
        <#assign otherInformation>
          ${workAreaItem.submittedDateTime()} <br/> ${workAreaItem.submittedBy()}
        </#assign>
      <@fdsResultList.resultListDataValue key="Consent type" value=consentType/>
      <@fdsResultList.resultListDataValue key="Location" value=location/>
      <@fdsResultList.resultListDataValue key="Status" value=workAreaItem.status()/>
      <@fdsResultList.resultListDataValue key="Other information" value=otherInformation/>
    </@fdsResultList.resultListDataItem>
  </@fdsResultList.resultListItem>
</#macro>
