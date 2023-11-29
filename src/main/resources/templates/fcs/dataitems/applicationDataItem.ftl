<#include '../layout/layout.ftl'/>

<#macro applicationResultListItem dataItem>
  <#assign tagContent>
    <#if dataItem.withdrawalOpen()!false>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Withdrawal requested"/>
      <br/>
    </#if>
    <#if dataItem.applicationUpdateOpen()!false>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Update due by ${dataItem.applicationUpdateDeadline()}"/>
      <br/>
    </#if>
    <#if dataItem.technicalReviewOpen()!false>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Technical review due by ${dataItem.technicalReviewDeadline()}"/>
      <br/>
    </#if>
    <#if dataItem.consultationFurtherInformationOpen()!false>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Further information requested"/>
      <br/>
    </#if>
    <#if dataItem.consultationOpen()!false>
      <@fdsResultList.resultListTag tagClass="govuk-tag--blue" tagText="Consultation due by ${dataItem.consultationDeadline()}"/>
      <br/>
    </#if>
  </#assign>
  <@fdsResultList.resultListItem
    linkHeadingText=dataItem.reference()
    linkHeadingUrl=springUrl(dataItem.url())
    captionHeadingText=dataItem.operator()
    itemTag=tagContent>
    <@fdsResultList.resultListDataItem>
      <#assign consentType>
        ${dataItem.type()}
        <br/>
        ${dataItem.duration()}
        <br/>
        ${dataItem.aceFlag()}
      </#assign>
      <#assign location>
        ${dataItem.asset()}
        <br/>
        ${dataItem.geographicArea()}
        <br/>
        ${dataItem.licenses()}
      </#assign>
      <#assign status>
        ${dataItem.status()}
        <br/>
        <#if dataItem.caseOfficer()?has_content>
          Case officer: ${dataItem.caseOfficer()}
          <br/>
        </#if>
        <#if dataItem.technicalReviewer()?has_content>
          Technical reviewer: ${dataItem.technicalReviewer()}
        </#if>
      </#assign>
      <#assign otherInformation>
        <#if dataItem.submittedDateTime()?has_content>
          Submitted: ${dataItem.submittedDateTime()}
          <br/>
        </#if>
        <#if dataItem.submittedBy()?has_content>
          Submitter: ${dataItem.submittedBy()}
        </#if>
      </#assign>
      <@fdsResultList.resultListDataValue key="Consent type" value=consentType/>
      <@fdsResultList.resultListDataValue key="Licence info" value=location/>
      <@fdsResultList.resultListDataValue key="Status" value=status/>
      <@fdsResultList.resultListDataValue key="Other information" value=otherInformation/>
    </@fdsResultList.resultListDataItem>
  </@fdsResultList.resultListItem>
</#macro>
