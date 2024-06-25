<#include '../layout/layout.ftl'>
<#import '../dataitems/applicationDataItem.ftl' as applicationDataItem>

<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
<#-- @ftlvariable name="fieldJson" type="uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson" -->

<#assign pageTitle = fieldJson.getName()/>

<#if !operatorExists || !licencesExist>
  <#assign warningBanner>
    <@grid.gridRow>
      <@grid.twoThirdsColumn>
        <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information">
          <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be started">
            This field has missing information
            <ul>
              <#if !operatorExists>
                <li>Field operator</li>
              </#if>
              <#if !licencesExist>
                <li>Associated licences</li>
              </#if>
            </ul>
            Contact <@mailTo.mailToLink mailToEmailAddress=customerBrandingConfigurationProperties.email() /> if you think the field should have this information.
          </@fdsNotificationBanner.notificationBannerContent>
        </@fdsNotificationBanner.notificationBannerInfo>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>
  </#assign>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.FULL_WIDTH
  notificationBannerContentOverride=warningBanner
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsDataItems.dataItem>
    <@fdsDataItems.dataValues key="Operator" value=operatorName/>
    <@fdsDataItems.dataValues key="Status" value=fieldJson.getStatusDisplayName()/>
    <@fdsDataItems.dataValues key="Geographic area" value=fieldJson.getGeographicArea().getDisplayName()/>
    <@fdsDataItems.dataValues key="Licences" value=licences/>
  </@fdsDataItems.dataItem>
  <#if startApplicationEnabled>
    <@fdsAction.link start=true linkText="Start application" linkUrl=springUrl(startApplicationUrl)/>
  </#if>
  <@fdsResultList.resultList resultCount=applicationDataItemViews?size resultCountSuffix="application">
    <#list applicationDataItemViews as dataItemView>
      <@applicationDataItem.applicationResultListItem dataItem=dataItemView/>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>
