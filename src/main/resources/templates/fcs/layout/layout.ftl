<#include '../../fds/layout.ftl'>
<#import '_pageSizes.ftl' as PageSize>
<#import '../macros/taskList.ftl' as taskList>

<#-- @ftlvariable name="serviceBranding" type="uk.co.nstauthority.fieldconsents.branding.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties" -->
<#-- @ftlvariable name="serviceHomeUrl" type="String" -->

<#macro defaultPage
  htmlTitle
  pageHeading=""
  caption=""
  phaseBanner=true
  pageSize=PageSize.TWO_THIRDS_COLUMN
  breadcrumbsMap={}
  errorItems=[]
  notificationBannerContent=""
>
  <#local serviceName = serviceBranding.name() />
  <#local customerMnemonic = customerBranding.mnemonic() />
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

  <@fdsDefaultPageTemplate
    htmlTitle=htmlTitle
    serviceName=serviceName
    htmlAppTitle=serviceName
    pageHeading=pageHeading
    caption=caption
    headerLogo="GOV_CREST"
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
    breadcrumbs=useBreadCrumbs
    breadcrumbsList=breadcrumbsMap
    errorItems=errorItems
    notificationBannerContent=notificationBannerContent
  >
    <#nested />
  </@fdsDefaultPageTemplate>
</#macro>