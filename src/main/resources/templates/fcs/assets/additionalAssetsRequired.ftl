<#include '../layout/layout.ftl'>
<#include '../flarevent/hintText.ftl'>

<#-- @ftlvariable name="successfulDeleteBanner" type="String" -->

<#assign pageTitle = "Additional fields"/>

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
  pageSize=PageSize.TWO_THIRDS_COLUMN
  notificationBannerContentOverride=deleteBanner>
  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
      path="form.otherAssetsRequired"
      labelText="Do you have any additional fields to add?"
      hintText=ADDITIONAL_ASSET_HINT_TEXT
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--xl">
      <@fdsRadio.radioYes path="form.otherAssetsRequired"/>
      <@fdsRadio.radioNo path="form.otherAssetsRequired"/>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
