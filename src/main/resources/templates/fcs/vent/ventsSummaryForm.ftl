<#include '../layout/layout.ftl'>
<#import '_ventSummary.ftl' as ventSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="ventViews" type="java.util.List<uk.co.nstauthority.fieldconsents.flarevent.vent.VentView>" -->

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
  notificationBannerContent=deleteBanner
>
  <#list ventViews as vent>
    <@ventSummary.ventSummary vent=vent showActions=true displayOrder="${vent.displayOrder}"/>
  </#list>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <#assign hasAddedAllVentsFormBind = "form.hasOtherVentsToAdd"/>
    <@fdsRadio.radioGroup
    path=hasAddedAllVentsFormBind
    labelText="Do you need to add another vent?">
      <@fdsRadio.radioYes path=hasAddedAllVentsFormBind/>
      <@fdsRadio.radioNo path=hasAddedAllVentsFormBind/>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>