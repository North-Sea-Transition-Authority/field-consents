<#include '../layout/layout.ftl'>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>
<#import '_expiringLicencesBanner.ftl' as expiringLicencesBanner>
<#import 'update/_applicationUpdateRequestHiddenSummary.ftl' as applicationUpdateRequestHiddenSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestView" -->

<#if !isSubmittable || !userHasPayAndSubmitPermission || expiringLicences?has_content>
  <#assign warningBanner>
    <#if !isSubmittable || !userHasPayAndSubmitPermission>
      <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information or permissions">
        <@fdsNotificationBanner.notificationBannerContent headingText="Application cannot be submitted">
          <#assign missingInformationExplanation="Not all mandatory sections in the application have been completed"/>
          <#assign missingPermissionsExplanation="Your account does not have permission to submit applications for the primary operator"/>
          <#if !isSubmittable && !userHasPayAndSubmitPermission>
            <ul>
              <li>${missingInformationExplanation}</li>
              <li>${missingPermissionsExplanation}</li>
            </ul>
          <#elseif !isSubmittable>
            ${missingInformationExplanation}
          <#elseif !userHasPayAndSubmitPermission>
            ${missingPermissionsExplanation}
          </#if>
        </@fdsNotificationBanner.notificationBannerContent>
      </@fdsNotificationBanner.notificationBannerInfo>
    </#if>
    <@expiringLicencesBanner.expiringLicencesBanner expiringLicences=expiringLicences/>
  </#assign>
</#if>
<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  caption=applicationReference!""
  notificationBannerContentOverride=warningBanner
  errorItems=errorList
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <@applicationSummary.applicationSummary accordionId=accordionId/>
    <#if isSubmittable && userHasPayAndSubmitPermission>
      <#if applicationUpdateRequestView?has_content>
        <@fdsRadio.radioGroup
          path="form.responseType"
          labelText="Describe the update"
          fieldsetHeadingSize="h2"
          fieldsetHeadingClass="govuk-fieldset__legend--l"
          hiddenContent=true>
          <@fdsRadio.radioItem
            path="form.responseType"
            itemMap={requestedChangesOnlyRadio.toString(): requestedChangesOnlyRadio.getDisplayName()} isFirstItem=true/>
          <@fdsRadio.radioItem
            path="form.responseType"
            itemMap={otherChangesRadio.toString(): otherChangesRadio.getDisplayName()}>
            <@fdsTextarea.textarea
              path="form.otherChangesDescription.inputValue"
              nestingPath="form.responseType"
              labelText=otherChangesRadio.getResponseTextLabel()
              hintText=otherChangesRadio.getResponseTextLabelHint()/>
          </@fdsRadio.radioItem>
        </@fdsRadio.radioGroup>
        <@applicationUpdateRequestHiddenSummary.applicationUpdateRequestHiddenSummary applicationUpdateRequestView=applicationUpdateRequestView!""/>
      </#if>
      <#if paymentRequired>
        <#assign submitButtonText="Pay and submit"/>
      <#else>
        <@fdsInsetText.insetText>No payment is required for this application.</@fdsInsetText.insetText>
        <#assign submitButtonText="Submit"/>
      </#if>
      <@fdsAction.submitButtons
        primaryButtonText=submitButtonText
        secondaryLinkText="Back to application"
        linkSecondaryAction=true
        linkSecondaryActionUrl="${springUrl(backLinkUrl)}"
      />
    <#else>
      <@fdsAction.link linkText="Back to application" linkUrl="${springUrl(backLinkUrl)}"/>
    </#if>
  </@fdsForm.htmlForm>
</@defaultPage>
