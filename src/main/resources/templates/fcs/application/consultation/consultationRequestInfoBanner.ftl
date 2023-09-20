<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="consultationRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView" -->

<#macro consulationInformationBanner consultationRequestView={}>
  <#if consultationRequestView?has_content>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Consultation information">
      <#assign headingText="Consultation due by ${consultationRequestView.deadline()}">
      <@fdsNotificationBanner.notificationBannerContent headingText=headingText/>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
