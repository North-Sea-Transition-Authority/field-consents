<#include '../../../layout/layout.ftl'>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#-- @ftlvariable name="furtherInformationView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView" -->

<#macro requestDetailsCard furtherInformationView>
  <@fdsSummaryList.summaryListCard headingText="Further information request details" summaryListId="further-information-request-detail-card">
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested by">
      ${furtherInformationView.requestedByUser()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested on">
      ${furtherInformationView.requestedAtTimestamp()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Request text">
      ${furtherInformationView.requestText()!""}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro requestNotificationBanner furtherInformationView>
  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Further information requested">
    <@fdsNotificationBanner.notificationBannerContent
      headingText="Further information requested"
      moreContent=furtherInformationView.requestText()/>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>
