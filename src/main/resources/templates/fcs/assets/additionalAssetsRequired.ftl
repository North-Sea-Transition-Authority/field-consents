<#include '../layout/layout.ftl'>

<#assign pageTitle = "Do you have any additional fields to add?"/>

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
pageSize=PageSize.TWO_THIRDS_COLUMN
notificationBannerContent=deleteBanner
>

  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
    path="form.otherAssetsRequired"
    labelText="">
      <@fdsRadio.radioYes path="form.otherAssetsRequired"/>
      <@fdsRadio.radioNo path="form.otherAssetsRequired"/>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>

</@defaultPage>