<#include '../../fds/layout.ftl'>
<#include '../../fds/objects/layouts/leftSubNavLayout.ftl'>
<#import '_pageSizes.ftl' as PageSize>
<#import '../macros/taskList.ftl' as taskList>
<#import '_header.ftl' as pageHeader>

<#-- @ftlvariable name="serviceBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties" -->
<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
<#-- @ftlvariable name="serviceHomeUrl" type="String" -->
<#-- @ftlvariable name="singleErrorMessage" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="flash" type="uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner" -->

<#assign SERVICE_NAME = serviceBrandingConfigurationProperties.name() />
<#assign CUSTOMER_MNEMONIC = customerBrandingConfigurationProperties.mnemonic() />
<#assign SERVICE_HOME_URL = springUrl(serviceHomeUrl) />

<#macro defaultPage
  htmlTitle
  pageHeading=""
  caption=""
  phaseBanner=true
  pageSize=PageSize.TWO_THIRDS_COLUMN
  backLinkUrl=""
  backLinkWithBrowserBack=false
  breadcrumbsMap={}
  errorItems=[]
  notificationBannerContentOverride=""
  singleErrorMessage=""
  showNavigationItems=true
>
  <#local serviceName = serviceBrandingConfigurationProperties.name() />
  <#local customerMnemonic = customerBrandingConfigurationProperties.mnemonic() />
  <#local serviceHomeUrl = springUrl(serviceHomeUrl) />

  <#assign fullPageWidth=false />
  <#assign fullWidthColumn=false />
  <#assign oneHalfColumn=false />
  <#assign oneThirdColumn=false />
  <#assign twoThirdsColumn=false />
  <#assign twoThirdsOneThirdColumn=false />
  <#assign oneQuarterColumn=false />

  <#if pageSize == PageSize.FULL_PAGE_WIDTH>
    <#assign fullPageWidth=true/>
  <#elseif pageSize == PageSize.FULL_WIDTH>
    <#assign fullWidthColumn=true/>
  <#elseif pageSize == PageSize.ONE_HALF_COLUMN>
    <#assign oneHalfColumn=true/>
  <#elseif pageSize == PageSize.ONE_THIRD_COLUMN>
    <#assign oneThirdColumn=true/>
  <#elseif pageSize == PageSize.TWO_THIRDS_ONE_THIRD_COLUMN>
    <#assign twoThirdsOneThirdColumn=true/>
  <#elseif pageSize == PageSize.ONE_QUARTER>
    <#assign oneQuarterColumn=true/>
  <#else>
    <#assign twoThirdsColumn=true/>
  </#if>

  <#assign useBreadCrumbs=false>
  <#if breadcrumbsMap?has_content>
    <#assign useBreadCrumbs=true>
  </#if>

  <#assign showBackLink = false>

  <#if backLinkUrl?has_content && useBreadCrumbs==false>
    <#assign showBackLink=true/>
  <#elseif backLinkWithBrowserBack == true && useBreadCrumbs == false>
    <#assign showBackLink=true/>
    <#assign backLinkUrl = ""/>
  </#if>

  <#-- if the notificationBannerContentOverride has no content then try and set from the flash data -->
  <#if notificationBannerContentOverride?has_content>
    <#assign notificationBannerContent=notificationBannerContentOverride/>
  <#else>
    <#assign notificationBannerContent>
      <@_flashNotificationBannerContent />
    </#assign>
  </#if>

  <#assign serviceHeader>
    <@_serviceHeader pageSize=pageSize />
  </#assign>

  <@fdsDefaultPageTemplate
    htmlTitle=htmlTitle
    serviceName=serviceName
    htmlAppTitle=serviceName
    pageHeading=pageHeading
    caption=caption
    headerContent=serviceHeader
    logoProductText=customerMnemonic
    phaseBanner=phaseBanner
    serviceUrl=serviceHomeUrl
    homePageUrl=serviceHomeUrl
    wrapperWidth=fullPageWidth
    fullWidthColumn=fullWidthColumn
    oneHalfColumn=oneHalfColumn
    oneThirdColumn=oneThirdColumn
    twoThirdsColumn=twoThirdsColumn
    twoThirdsOneThirdColumn=twoThirdsOneThirdColumn
    oneQuarterColumn=oneQuarterColumn
    topNavigation=showNavigationItems
    backLink=showBackLink
    backLinkUrl=backLinkUrl
    breadcrumbs=useBreadCrumbs
    breadcrumbsList=breadcrumbsMap
    singleErrorMessage=singleErrorMessage
    errorItems=errorItems
    notificationBannerContent=notificationBannerContent
  >
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>

<#macro defaultPageWithSubNavigation
  htmlTitle
  phaseBanner=true
  showNavigationItems=true
>
  <#local serviceName = serviceBrandingConfigurationProperties.name() />
  <#local customerMnemonic = customerBrandingConfigurationProperties.mnemonic() />
  <#local serviceHomeUrl = springUrl(serviceHomeUrl) />

  <#assign serviceHeader>
    <@_serviceHeader pageSize=PageSize.TWO_THIRDS_COLUMN />
  </#assign>

  <@fdsLeftSubNavPageTemplate
    htmlTitle=htmlTitle
    serviceName=serviceName
    htmlAppTitle=serviceName
    headerContent=serviceHeader
    logoProductText=customerMnemonic
    phaseBanner=phaseBanner
    serviceUrl=serviceHomeUrl
    homePageUrl=serviceHomeUrl
    topNavigation=showNavigationItems
  >
    <#nested />
  </@fdsLeftSubNavPageTemplate>
</#macro>

<#macro defaultPageWithSubNavigationSubNav smallSubnav=false>
  <@fdsLeftSubNavPageTemplateSubNav smallSubnav=smallSubnav>
    <#nested />
  </@fdsLeftSubNavPageTemplateSubNav>
</#macro>

<#macro defaultPageWithSubNavigationContent pageHeading="" notificationBannerContentOverride="">
  <#-- if the notificationBannerContentOverride has no content then try and set from the flash data -->
  <#if notificationBannerContentOverride?has_content>
    <#assign notificationBannerContent=notificationBannerContentOverride/>
  <#else>
    <#assign notificationBannerContent>
      <@_flashNotificationBannerContent />
    </#assign>
  </#if>

  <@fdsLeftSubNavPageTemplateContent pageHeading=pageHeading notificationBannerContent=notificationBannerContent>
    <#nested />
  </@fdsLeftSubNavPageTemplateContent>
</#macro>

<#macro _serviceHeader pageSize>
  <@pageHeader.header
    serviceName=SERVICE_NAME
    customerMnemonic=CUSTOMER_MNEMONIC
    serviceHomeUrl=SERVICE_HOME_URL
    signedInUserName=(loggedInUser?has_content)?then(loggedInUser.displayName(), "")
    signOutUrl=springUrl("/logout")
    pageSize=pageSize
  />
</#macro>

<#macro _flashNotificationBannerContent>
  <#if flash?has_content>
    <#local bannerContent>
      <#if flash.headingContent?has_content>
        <#if flash.otherContent?has_content>
          <@fdsNotificationBanner.notificationBannerContent headingText=flash.headingContent moreContent=flash.otherContent/>
        <#else>
          <@fdsNotificationBanner.notificationBannerContent>${flash.headingContent}</@fdsNotificationBanner.notificationBannerContent>
        </#if>
      <#else>
        <p class="govuk-body">
          ${flash.otherContent}
        </p>
      </#if>
    </#local>

    <#if flash.type.name() == "INFO">
      <@fdsNotificationBanner.notificationBannerInfo bannerTitleText=flash.title>
        ${bannerContent}
      </@fdsNotificationBanner.notificationBannerInfo>
    <#elseif flash.type.name() == "SUCCESS">
      <@fdsNotificationBanner.notificationBannerSuccess bannerTitleText=flash.title>
        ${bannerContent}
      </@fdsNotificationBanner.notificationBannerSuccess>
    </#if>
  </#if>
</#macro>
