<#include '../layout/layout.ftl'>
<#include '../flarevent/hintText.ftl'>
<#import '_ventSummary.ftl' as ventSummary>

<#-- @ftlvariable name="successfulDeleteBanner" type="String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="ventViews" type="java.util.List<uk.co.nstauthority.fieldconsents.flarevent.vent.vents.VentView>" -->

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
  <#list ventViews as vent>
    <@ventSummary.ventSummary vent=vent showActions=true displayOrder="${vent.displayOrder}"/>
  </#list>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <#assign hasAddedAllVentsFormBind = "form.hasOtherVentsToAdd"/>
    <@fdsRadio.radioGroup
      path=hasAddedAllVentsFormBind
      hintText=ADD_VENT_HINT_TEXT
      labelText="Do you need to add another vent system?"
      fieldsetHeadingClass="govuk-fieldset__legend--m">
      <@fdsRadio.radioYes path=hasAddedAllVentsFormBind/>
      <@fdsRadio.radioNo path=hasAddedAllVentsFormBind/>
    </@fdsRadio.radioGroup>
    <@fdsAction.button buttonText="Save and continue"/>
  </@fdsForm.htmlForm>
</@defaultPage>
