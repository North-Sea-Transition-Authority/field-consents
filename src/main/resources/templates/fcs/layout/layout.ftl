<#include '../../fds/layout.ftl'>
<#include '../../fds/objects/layouts/leftSubNavLayout.ftl'>
<#include '../../fds/components/header/energyPortalHeader.ftl'>
<#import '_pageSizes.ftl' as PageSize>
<#import '../macros/taskList.ftl' as taskList>
<#import '../macros/_multiLineText.ftl' as multiLineText>
<#import '../macros/mailTo.ftl' as mailTo>
<#import '../macros/requestNewCompany.ftl' as requestNewCompany>

<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
<#-- @ftlvariable name="serviceBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties" -->
<#-- @ftlvariable name="singleErrorMessage" type="String" -->
<#-- @ftlvariable name="loggedInUser" type="uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail" -->
<#-- @ftlvariable name="flash" type="uk.co.nstauthority.fieldconsents.fds.notificationbanner.NotificationBanner" -->
<#-- @ftlvariable name="footerLinks" type="java.util.List<uk.co.nstauthority.fieldconsents.fds.footer.FooterLink>" -->
<#-- @ftlvariable name="feedbackUrl" type="String" -->
<#-- @ftlvariable name="cookiesStatementUrl" type="String" -->
<#-- @ftlvariable name="workAreaUrl" type="String" -->

<#macro defaultPage
  htmlTitle
  pageHeading=""
  caption=""
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

  <#assign customScriptContent>
    <script type="module" src="<@spring.url'/assets/static/js/fcs-bundle.js'/>"></script>
  </#assign>

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
    <@_serviceHeader
      wrapperWidth=fullPageWidth
      loggedInUser=loggedInUser
    />
  </#assign>

  <#assign footerContent>
    <@_footer isFullPageWidth=fullPageWidth/>
  </#assign>

  <@fdsDefaultPageTemplate
    htmlTitle=htmlTitle
    serviceName=serviceName
    htmlAppTitle=serviceName
    pageHeading=pageHeading
    caption=caption
    headerContent=serviceHeader
    topNavigationServiceName=serviceBrandingConfigurationProperties.name()
    topNavigationServiceUrl=springUrl(workAreaUrl)
    logoProductText=customerMnemonic
    phaseBanner=false
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
    customScriptContent=customScriptContent
    singleErrorMessage=singleErrorMessage
    errorItems=errorItems
    notificationBannerContent=notificationBannerContent
    footerContent=footerContent
    cookieBannerMacro=_cookieBanner
  >
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>

<#macro defaultPageWithSubNavigation
  htmlTitle
  showNavigationItems=true
  backLinkUrl=""
>
  <#local serviceName = serviceBrandingConfigurationProperties.name() />
  <#local customerMnemonic = customerBrandingConfigurationProperties.mnemonic() />

  <#assign serviceHeader>
    <@_serviceHeader loggedInUser=loggedInUser/>
  </#assign>

  <#assign footerContent>
    <@_footer/>
  </#assign>

  <@fdsLeftSubNavPageTemplate
    htmlTitle=htmlTitle
    serviceName=serviceName
    htmlAppTitle=serviceName
    headerContent=serviceHeader
    topNavigationServiceName=serviceBrandingConfigurationProperties.name()
    topNavigationServiceUrl=springUrl(workAreaUrl)
    logoProductText=customerMnemonic
    phaseBanner=false
    topNavigation=showNavigationItems
    footerContent=footerContent
    cookieBannerMacro=_cookieBanner
    backLink=backLinkUrl?has_content
    backLinkUrl=backLinkUrl
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

<#macro _serviceHeader wrapperWidth=false loggedInUser="">
  <@energyPortalHeader
    userDisplayName=loggedInUser?has_content?then(loggedInUser.displayNameIncludingAnyProxyUser(), "")
    wrapperWidth=wrapperWidth
    headerLogo="NSTA"
    signOutUrl=springUrl("/logout")
  />
</#macro>

<#macro _footer isFullPageWidth=false>
  <#local footerMetaContent>
    <@fdsFooter.footerMeta footerMetaHiddenHeading="Footer links">
      <#list footerLinks as footerLink>
        <@fdsFooter.footerMetaLink linkText=footerLink.getDisplayText() linkUrl=springUrl(footerLink.getUrl())/>
      </#list>
    </@fdsFooter.footerMeta>
  </#local>
  <@fdsNstaFooter.nstaFooter wrapperWidth=isFullPageWidth metaLinks=true footerMetaContent=footerMetaContent/>
</#macro>

<#macro _cookieBanner>
  <@fdsCookieBanner.analyticsCookieBanner
    serviceName=serviceBrandingConfigurationProperties.name()
    cookieSettingsUrl=springUrl(cookiesStatementUrl)
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
