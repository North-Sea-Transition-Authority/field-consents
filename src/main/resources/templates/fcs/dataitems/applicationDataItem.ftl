<#include '../layout/layout.ftl'/>

<#macro applicationResultListItem dataItem>
  <#assign tagContent>
    <#if dataItem.withdrawalOpen()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">Withdrawal requested</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.applicationUpdateOpen()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">Update due by ${dataItem.applicationUpdateDeadline()}</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.technicalReviewOpen()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">Technical review due by ${dataItem.technicalReviewDeadline()}</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.consultationFurtherInformationOpen()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">Further information requested</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.consultationOpen()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">Consultation due by ${dataItem.consultationDeadline()}</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.approvedForIssue()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">Ready to grant and issue</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.consentStatus()?has_content>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--blue">${dataItem.consentStatus().getDisplayName()}</@fdsTag.tag>
      <br/>
    </#if>
    <#if dataItem.consentBreachOpen()!false>
      <@fdsTag.tag tagClass="govuk-tag--multiple govuk-tag--red">Consent exceeded</@fdsTag.tag>
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
        <#if dataItem.aceFlag()!false>
          <@fdsTag.tag tagClass="govuk-tag--red">${dataItem.aceFlagText()}</@fdsTag.tag>
        <#else>
          ${dataItem.aceFlagText()}
        </#if>
      </#assign>
      <#assign location>
        ${dataItem.asset()}
        <br/>
        ${dataItem.geographicArea()}
        <br/>
        ${dataItem.licenses()!""} <!-- cope with migrated data -->
      </#assign>
      <#assign status>
        ${dataItem.status()}
        <br/>
        <#if dataItem.caseOfficer()?has_content>
          Case officer: ${dataItem.caseOfficer()}
          <br/>
        </#if>
        <#if dataItem.camUser()?has_content>
          CAM: ${dataItem.camUser()}
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
