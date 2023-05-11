<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<#if !isSubmittable || !userHasSubmitPermission>
  <#assign warningBanner>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information or permissions">
      <@fdsNotificationBanner.notificationBannerContent headingText="Application cannot be submitted">
        <#assign missingInformationExplanation="Not all mandatory sections shown on the task list have been completed"/>
        <#assign missingPermissionsExplanation="Your account does not have permission to submit applications for the primary operator"/>
        <#if !isSubmittable && !userHasSubmitPermission>
          <ul>
            <li>${missingInformationExplanation}</li>
            <li>${missingPermissionsExplanation}</li>
          </ul>
        <#elseif !isSubmittable>
          ${missingInformationExplanation}
        <#elseif !userHasSubmitPermission>
          ${missingPermissionsExplanation}
        </#if>
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#assign>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  notificationBannerContentOverride=warningBanner
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <@applicationSummary.applicationSummary accordionId=accordionId/>
    <#if isSubmittable && userHasSubmitPermission>
      <@fdsAction.submitButtons
        primaryButtonText="Submit"
        secondaryLinkText="Back to task list"
        linkSecondaryAction=true
        linkSecondaryActionUrl="${springUrl(backLinkUrl)}"/>
    <#else>
      <@fdsAction.link linkText="Back to task list" linkUrl="${springUrl(backLinkUrl)}"/>
    </#if>
  </@fdsForm.htmlForm>
</@defaultPage>
