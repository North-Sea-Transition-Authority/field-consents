<#include '../layout/layout.ftl'>

<#macro consentBreachedBanner consentBreached>
  <#if consentBreached>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Information">
      <@fdsNotificationBanner.notificationBannerContent>
        The consent has been exceeded
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
