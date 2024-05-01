<#include '../../../layout/layout.ftl'>
<#import '../../../../fds/components/summaryList/summaryList.ftl' as fdsSummaryList>

<#-- @ftlvariable name="furtherInformationView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView" -->

<#macro requestDetailsCard furtherInformationView>
  <@fdsSummaryList.summaryListCard headingText="Further information request details" summaryListId="further-information-request-detail-card">
    <@_requestDetails furtherInformationView=furtherInformationView/>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro detailsCard furtherInformationView index>
  <#assign headingText="Further information request ${index}">
  <#assign summaryListId="further-information-request-card-[${index}]">
  <@fdsSummaryList.summaryListCard headingText=headingText summaryListId=summaryListId>
    <@fdsSummaryList.summaryListRowNoAction keyText="Status">
      <#if furtherInformationView.isClosed()>
        Closed
      <#else>
        Open
      </#if>
    </@fdsSummaryList.summaryListRowNoAction>
    <@_requestDetails furtherInformationView=furtherInformationView/>
    <#if furtherInformationView.isClosed()!false>
      <@_responseDetails furtherInformationView=furtherInformationView/>
    </#if>
  </@fdsSummaryList.summaryListCard>
</#macro>

<#macro requestNotificationBanner furtherInformationView>
  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Further information requested">
  <#local bannerText>
    <@multiLineText.multiLineText contentText=furtherInformationView.requestText()/>
  </#local>
    <@fdsNotificationBanner.notificationBannerContent
      headingText="Further information requested"
      moreContent=bannerText/>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>

<#macro _requestDetails furtherInformationView>
  <@fdsSummaryList.summaryListRowNoAction keyText="Requested by">
    ${furtherInformationView.requestedByUser()!""}
  </@fdsSummaryList.summaryListRowNoAction>
  <@fdsSummaryList.summaryListRowNoAction keyText="Requested on">
    ${furtherInformationView.requestedAtTimestamp()!""}
  </@fdsSummaryList.summaryListRowNoAction>
  <@fdsSummaryList.summaryListRowNoAction keyText="Request text">
    <@multiLineText.multiLineText contentText=furtherInformationView.requestText()!""/>
  </@fdsSummaryList.summaryListRowNoAction>
</#macro>

<#macro _responseDetails furtherInformationView>
  <@fdsSummaryList.summaryListRowNoAction keyText="Responded by">
    ${furtherInformationView.respondedByUser()!""}
  </@fdsSummaryList.summaryListRowNoAction>
  <@fdsSummaryList.summaryListRowNoAction keyText="Responded on">
    ${furtherInformationView.respondedAtTimestamp()!""}
  </@fdsSummaryList.summaryListRowNoAction>
  <@fdsSummaryList.summaryListRowNoAction keyText="Response text">
    <@multiLineText.multiLineText contentText=furtherInformationView.responseText()!""/>
  </@fdsSummaryList.summaryListRowNoAction>
</#macro>
