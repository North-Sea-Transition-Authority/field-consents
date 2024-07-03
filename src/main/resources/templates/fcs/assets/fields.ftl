<#include '../layout/layout.ftl'>
<#import '../dataitems/applicationDataItem.ftl' as applicationDataItem>
<#import '_startApplicationDecisionBanner.ftl' as startApplicationDecisionBanner>

<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
<#-- @ftlvariable name="fieldJson" type="uk.co.nstauthority.fieldconsents.assets.fields.FieldWithOperatorAndLicencesJson" -->

<#assign pageTitle = fieldJson.getName()/>

<#if !startApplicationDecision.canBeStarted()>
  <#assign warningBanner>
    <@startApplicationDecisionBanner.banner startApplicationDecision/>
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
  <#if startApplicationDecision.canBeStarted()>
    <@fdsAction.link start=true linkText="Start application" linkUrl=springUrl(startApplicationUrl)/>
  </#if>
  <@fdsResultList.resultList resultCount=applicationDataItemViews?size resultCountSuffix="application">
    <#list applicationDataItemViews as dataItemView>
      <@applicationDataItem.applicationResultListItem dataItem=dataItemView/>
    </#list>
  </@fdsResultList.resultList>
</@defaultPage>
