<#import '../../../../fds/components/notificationBanner/notificationBanner.ftl' as fdsNotificationBanner>

<#macro requestBanner furtherInformationRequestView={}>
  <#if furtherInformationRequestView?has_content>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Further information requested">
      <@fdsNotificationBanner.notificationBannerContent
        headingText="Further information requested"
        moreContent=furtherInformationRequestView.requestText()/>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
