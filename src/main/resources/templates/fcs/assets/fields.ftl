<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties" -->

<#assign pageTitle = fieldName/>

<#if noOperatorExists || noLicencesExist>
  <#assign warningBanner>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information">
      <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be started">
        This field has missing information
        <ul>
          <#if noOperatorExists>
            <li>Field operator</li>
          </#if>
          <#if noLicencesExist>
            <li>Associated licences</li>
          </#if>
        </ul>
        Contact the ${customerBranding.mnemonic()} if you think the field should have this information.
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
  <#if startApplicationEnabled>
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