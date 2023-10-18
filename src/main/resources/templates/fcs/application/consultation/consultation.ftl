<#include '../../layout/layout.ftl'>
<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#-- @ftlvariable name="consultationRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView" -->

<#macro requestDetailsCard consultationRequestView={}>
  <#if consultationRequestView?has_content>
    <@fdsSummaryList.summaryListCard headingText="Consultation details" summaryListId="consultation-detail-card">
      <@fdsSummaryList.summaryListRowNoAction keyText="Deadline">
        ${consultationRequestView.deadline()}
      </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryListCard>
  </#if>
</#macro>

<#macro consulationInformationBanner consultationRequestView={}>
  <#if consultationRequestView?has_content>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Consultation information">
      <#assign headingText="Consultation due by ${consultationRequestView.deadline()}">
      <@fdsNotificationBanner.notificationBannerContent headingText=headingText/>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
