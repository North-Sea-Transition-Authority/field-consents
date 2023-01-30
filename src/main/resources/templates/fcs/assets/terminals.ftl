<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties" -->

<#assign pageTitle = terminalName/>

<#if noOperatorExists>
  <#assign warningBanner>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information">
      <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be started">
        This facility has missing information
        <ul>
          <li>Facility operator</li>
        </ul>
        Contact the ${customerBranding.mnemonic()} if you think the facility should have this information.
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
  <#if !noOperatorExists>
    <@fdsStartPage.startPage
      startActionText="Start application"
      startActionUrl=springUrl(startApplicationUrl)
      startActionButton=false>
      <p class="govuk-body">
        This page will allow you to work with the facility in question.
      </p>
    </@fdsStartPage.startPage>
  </#if>
</@defaultPage>