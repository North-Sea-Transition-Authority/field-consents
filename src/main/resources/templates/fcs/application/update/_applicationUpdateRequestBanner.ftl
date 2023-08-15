<#include '../../layout/layout.ftl'>
<#import '_applicationUpdateRequestHiddenSummary.ftl' as applicationUpdateRequestHiddenSummary>

<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestView" -->

<#macro applicationUpdateRequestBanner applicationUpdateRequestView>
  <#if applicationUpdateRequestView?has_content>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Update requested">
      <#assign infoBannerContent>
        <@applicationUpdateRequestHiddenSummary.applicationUpdateRequestHiddenSummary applicationUpdateRequestView=applicationUpdateRequestView/>
      </#assign>
      <@fdsNotificationBanner.notificationBannerContent headingText="Update your application to provide the requested information" moreContent=infoBannerContent/>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
