<#include '../layout/layout.ftl'>
<#import '../dataitems/applicationDataItem.ftl' as applicationDataItem>

<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
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
        Contact ${customerBrandingConfigurationProperties.email()} if you think the facility should have this information.
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerInfo>
  </#assign>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
  notificationBannerContentOverride=warningBanner
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsDataItems.dataItem>
    <@fdsDataItems.dataValues key="Operator" value=operatorName/>
    <@fdsDataItems.dataValues key="Status" value=terminalJson.getStatusDisplayName()/>
  </@fdsDataItems.dataItem>
  <#if startApplicationEnabled>
    <@fdsAction.link start=true linkText="Start application" linkUrl=springUrl(startApplicationUrl)/>
  </#if>
  <@fdsResultList.resultList resultCount=applicationDataItems?size resultCountSuffix="application">
    <#list applicationDataItems as dataItem>
      <@applicationDataItem.applicationResultListItem dataItem=dataItem/>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>
