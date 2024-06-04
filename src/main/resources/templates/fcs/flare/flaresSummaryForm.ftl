<#include '../layout/layout.ftl'>
<#include '../flarevent/hintText.ftl'>
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
      hintText=ADD_FLARE_HINT_TEXT
      labelText="Do you need to add another flare system?"
      fieldsetHeadingClass="govuk-fieldset__legend--m">
      <@fdsRadio.radioYes path=hasAddedAllFlaresFormBind/>
      <@fdsRadio.radioNo path=hasAddedAllFlaresFormBind/>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>
