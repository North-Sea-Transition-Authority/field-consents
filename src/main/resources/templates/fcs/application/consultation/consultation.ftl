<#include '../../layout/layout.ftl'>
<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>
<#import '../../summary/_summaryDetails.ftl' as summaryDetails>

<#-- @ftlvariable name="consultationRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView" -->

<#macro requestDetailsCard consultationRequestView>
  <@fdsSummaryList.summaryListCard headingText="Consultation details" summaryListId="consultation-detail-card">
    <@fdsSummaryList.summaryListRowNoAction keyText="Deadline">
      ${consultationRequestView.deadline()}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro notificationBanner consultationRequestView>
  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Consultation information">
    <#assign headingText="Consultation due by ${consultationRequestView.deadline()}">
    <@fdsNotificationBanner.notificationBannerContent headingText=headingText/>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>

<#macro consultationList consultationSummaryItems>
  <#if consultationSummaryItems?has_content>
    <#list consultationSummaryItems as summaryItem>
      <h2 class="govuk-heading-l">${summaryItem.displayName()}</h2>
      <@summaryDetails.summaryDetails summaryItem=summaryItem/>
    </#list>
  <#else>
    <@fdsInsetText.insetText>
      No consultations have taken place on this case.
    </@fdsInsetText.insetText>
  </#if>
</#macro>
