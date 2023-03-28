<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="terminalJson" type="uk.co.nstauthority.fieldconsents.assets.terminals.TerminalWithOperatorJson" -->

<#assign pageTitle = terminalJson.getName()/>

<#if !operatorExists>
  <#assign warningBanner>
    <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information">
      <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be started">
        This facility has missing information
        <ul>
          <li>Facility operator</li>
        </ul>
        Contact ${customerBranding.email()} if you think the facility should have this information.
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
  <@fdsDataItems.dataItem>
    <@fdsDataItems.dataValues key="Operator" value=operatorName/>
    <@fdsDataItems.dataValues key="Status" value=terminalJson.getStatusDisplayName()/>
  </@fdsDataItems.dataItem>
  <#if operatorExists>
    <@fdsAction.link start=true linkText="Start application" linkUrl=springUrl(startApplicationUrl)/>
  </#if>
</@defaultPage>