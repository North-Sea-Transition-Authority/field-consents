<#include '../../../layout/layout.ftl'>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>
<#import '../../consent/documents/_consentFilesSummary.ftl' as consentFilesSummary>
<#import '../../consent/data/_consentDataSummary.ftl' as consentDataSummary>
<#import '../../consent/_fieldEquityPartner.ftl' as fieldEquityPartner>

<#-- @ftlvariable name="consentTabConsentSummaryView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.ConsentTabConsentSummaryView" -->

<#macro tab showIssuedByUser consentTabConsentSummaryView="">
  <#if !consentTabConsentSummaryView?has_content>
    <@fdsInsetText.insetText>
      A consent has not yet been issued for this application.
    </@fdsInsetText.insetText>
  <#else>
    <@fdsSummaryList.summaryListCard headingText="Consent information" summaryListId="consent-card">
      <#if showIssuedByUser>
        <@fdsSummaryList.summaryListRowNoAction keyText="Consent issued by">
          ${consentTabConsentSummaryView.issuedByUser()}
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>
      <@fdsSummaryList.summaryListRowNoAction keyText="Consent issued on">
        ${consentTabConsentSummaryView.formattedIssuedDate()}
      </@fdsSummaryList.summaryListRowNoAction>
      <@fdsSummaryList.summaryListRowNoAction keyText="Consent status">
        <strong class="govuk-tag govuk-tag--blue">
          ${consentTabConsentSummaryView.consentStatus().getDisplayName()}
        </strong>
      </@fdsSummaryList.summaryListRowNoAction>
      <#if consentTabConsentSummaryView.consentSupersededByApplicationReference()?has_content>
        <@fdsSummaryList.summaryListRowNoAction keyText="Consent superseded by">
          ${consentTabConsentSummaryView.consentSupersededByApplicationReference()}
        </@fdsSummaryList.summaryListRowNoAction>
      </#if>

      <@consentDataSummary.summaryCardContent
        applicationType=consentTabConsentSummaryView.applicationType()
        consentLengthType=consentTabConsentSummaryView.consentLengthType()
        consentDataView=consentTabConsentSummaryView.consentDataView()
        consentFigureUnitView=consentTabConsentSummaryView.consentFigureUnitView()/>
      <#if consentTabConsentSummaryView.consentFieldEquityPartnersView()?has_content>
        <@fieldEquityPartner.consentFepsSummaryCardContent
          consentFieldEquityPartnersView=consentTabConsentSummaryView.consentFieldEquityPartnersView()/>
      </#if>
    </@fdsSummaryList.summaryListCard>

    <@consentFilesSummary.summary heading="Consent documents" fileViews=consentTabConsentSummaryView.summaryFileViews() />
  </#if>
</#macro>
