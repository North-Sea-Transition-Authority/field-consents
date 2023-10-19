<#include '../../../layout/layout.ftl'>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#-- @ftlvariable name="furtherInformationRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformationrequest.FurtherInformationRequestView" -->

<#macro requestDetailsCard furtherInformationRequestView>
  <@fdsSummaryList.summaryListCard headingText="Further information request details" summaryListId="further-information-request-detail-card">
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested by">
      ${furtherInformationRequestView.requestedByUser()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Requested on">
      ${furtherInformationRequestView.requestedAtTimestamp()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Request text">
      ${furtherInformationRequestView.requestText()!""}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro requestNotificationBanner furtherInformationRequestView>
  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Further information requested">
    <@fdsNotificationBanner.notificationBannerContent
      headingText="Further information requested"
      moreContent=furtherInformationRequestView.requestText()/>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>
