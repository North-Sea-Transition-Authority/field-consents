<#include '../layout/layout.ftl'>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
>
  <@fdsSearch.searchPage>
    <@fdsSearch.searchFilter>
      <@fdsSearch.searchFilterList clearFilterUrl=springUrl(clearFiltersUrl) filterButtonClass="govuk-button govuk-button--secondary">
        <#--<@referenceFilter form=form/>--> <#--TODO: Uncomment this out when case reference ticket FCS-323 is resolved -->
        <@statusFilter form=form statusCheckboxes=appStatuses/>
        <@applicationTypeFilter form=form applicationTypeCheckboxes=appTypes/>
      </@fdsSearch.searchFilterList>
    </@fdsSearch.searchFilter>
    <@fdsSearch.searchPageContent>
      <@fdsResultList.resultList resultCount=workAreaItems?size>
        <#list workAreaItems as workAreaItem>
          <@fcsWorkAreaItem workAreaItem=workAreaItem/>
        </#list>
      </@fdsResultList.resultList>
    </@fdsSearch.searchPageContent>
  </@fdsSearch.searchPage>
</@defaultPage>

<#macro referenceFilter form>
  <@fdsSearch.searchFilterItem itemName="Reference" expanded=form.referenceSearchTerm?has_content>
    <@fdsSearch.searchTextInput
      path="form.referenceSearchTerm"
      labelText=""
      suffixScreenReaderPrompt="Application reference"
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

<#macro fcsWorkAreaItem workAreaItem>
  <@fdsResultList.resultListItem
    linkHeadingText=workAreaItem.reference()
    linkHeadingUrl=springUrl(workAreaItem.url())
    captionHeadingText=workAreaItem.operator()
  >
    <@fdsResultList.resultListDataItem>
        <#assign consentType>
          ${workAreaItem.type()} <br/> ${workAreaItem.duration()}
        </#assign>
        <#assign location>
          ${workAreaItem.asset()} <br/> ${workAreaItem.seaLocation()}
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