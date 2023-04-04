<#include '../layout/layout.ftl'>
<#import './_flareSummary.ftl' as flareSummary>

<#-- @ftlvariable name="successfulDeleteBanner" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="flareViews" type="java.util.List<uk.co.nstauthority.fieldconsents.flarevent.flare.flares.FlareView>" -->

<#if successfulDeleteBanner?has_content>
  <#assign deleteBanner>
    <@fdsNotificationBanner.notificationBannerSuccess bannerTitleText="Success">
      <@fdsNotificationBanner.notificationBannerContent>
        ${successfulDeleteBanner}
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerSuccess>
  </#assign>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList
  notificationBannerContentOverride=deleteBanner
>
  <#list flareViews as flare>
    <@flareSummary.flareSummary flare=flare showActions=true displayOrder="${flare.displayOrder}"/>
  </#list>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <#assign hasAddedAllFlaresFormBind = "form.hasOtherFlaresToAdd"/>
    <@fdsRadio.radioGroup
    path=hasAddedAllFlaresFormBind
    labelText="Do you need to add another flare?">
      <@fdsRadio.radioYes path=hasAddedAllFlaresFormBind/>
      <@fdsRadio.radioNo path=hasAddedAllFlaresFormBind/>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>