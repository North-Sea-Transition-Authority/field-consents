<#include '../layout/layout.ftl'>

<#macro withdrawalRequestedBanner openWithdrawal>
  <#if openWithdrawal>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Withdrawal requested">
      <@fdsNotificationBanner.notificationBannerContent>
        A withdrawal has been requested for this application
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#if>
</#macro>
