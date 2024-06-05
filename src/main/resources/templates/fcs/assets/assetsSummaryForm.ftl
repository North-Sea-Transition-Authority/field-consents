<#include '../layout/layout.ftl'>
<#include '../flarevent/hintText.ftl'>
<#import '_assetSummary.ftl' as assetSummary>

<#-- @ftlvariable name="successfulDeleteBanner" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="assetViews" type="java.util.List<uk.co.nstauthority.fieldconsents.application.assets.AssetView>" -->

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
  <#list assetViews as asset>
    <@assetSummary.assetSummary asset=asset showActions=true displayOrder="${asset.displayOrder()}"/>
  </#list>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <#assign hasAddedAllAssetsFormBind = "form.hasOtherAssetsToAdd"/>
    <@fdsRadio.radioGroup
      path=hasAddedAllAssetsFormBind
      labelText="Do you need to add another field?"
      fieldsetHeadingClass="govuk-fieldset__legend--m"
      hintText=ADDITIONAL_ASSET_HINT_TEXT>
      <@fdsRadio.radioYes path=hasAddedAllAssetsFormBind/>
      <@fdsRadio.radioNo path=hasAddedAllAssetsFormBind/>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>
