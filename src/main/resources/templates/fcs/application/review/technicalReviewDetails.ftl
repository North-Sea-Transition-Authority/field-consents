<#import '../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>
<#import '../../../fds/components/notificationBanner/notificationBanner.ftl' as fdsNotificationBanner>

<#-- @ftlvariable name="technicalReviewSummaryView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.technicalreview.TechnicalReviewSummaryView" -->

<#macro technicalReviewDetails technicalReviewSummaryView>
  <@fdsSummaryList.summaryListCard headingText="Review details" summaryListId="review-details-summary-card-list">
    <@fdsSummaryList.summaryListRowNoAction keyText="Review deadline">
      ${technicalReviewSummaryView.deadline()!""}
    </@fdsSummaryList.summaryListRowNoAction>
    <@fdsSummaryList.summaryListRowNoAction keyText="Review notes">
      ${technicalReviewSummaryView.note()!""}
    </@fdsSummaryList.summaryListRowNoAction>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro notificationBanner technicalReviewSummaryView>
  <#assign deadline = technicalReviewSummaryView.deadline()!""/>
  <#assign note = technicalReviewSummaryView.note()!""/>

  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Technical review required">
    <#if deadline?has_content>
      <@fdsNotificationBanner.notificationBannerContent headingText="Technical review due by ${deadline}" moreContent=note/>
    <#else>
      <@fdsNotificationBanner.notificationBannerContent headingText="Technical review due" moreContent=note/>
    </#if>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>
