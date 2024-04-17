<#import '../../../fds/components/notificationBanner/notificationBanner.ftl' as fdsNotificationBanner>

<#macro notificationBanner consentIssuingApprovalSummaryView>
  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Application ready to grant and issue">
    <@fdsNotificationBanner.notificationBannerContent
      headingText="Application marked as ready to grant and issue"
      moreContent="Marked by ${consentIssuingApprovalSummaryView.formattedApprovedByUser()} on ${consentIssuingApprovalSummaryView.formattedApprovedDate()}"/>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>
