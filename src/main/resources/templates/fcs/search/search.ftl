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
<#--         TODO: temporarely disabled until FCS-426 is fixed -->
<#--      <@dataItemFilter.operatorFilter form=form prefilledOperator=prefilledOperator operatorSearchRestUrl=operatorSearchRestUrl/>-->
    </@fdsSearch.searchFilterList>
  </@fdsSearch.searchFilter>
  <@fdsSearch.searchPageContent twoThirdsWidth=true>
    <#if showResults?has_content>
      <@fdsResultList.resultList resultCount=searchResultItems?size>
        <#list searchResultItems as searchResultItem>
          <@fcsApplicationDataItem dataItem=searchResultItem pageTitle=pageTitle/>
        </#list>
      </@fdsResultList.resultList>
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
