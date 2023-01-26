<#include '../layout/layout.ftl'>

<#assign pageTitle = fieldName/>

<#if !licencesExist>
  <#assign warningBanner>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText=warningHeading>
      <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be started">
        ${warningContent}
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#assign>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  notificationBannerContent=warningBanner
>
  <#if licencesExist>
    <@fdsStartPage.startPage
      startActionText="Start application"
      startActionUrl=springUrl(startApplicationUrl)
      startActionButton=false>
      <p class="govuk-body">
        This page will allow you to work with the field in question.
      </p>
    </@fdsStartPage.startPage>
  </#if>
</@defaultPage>