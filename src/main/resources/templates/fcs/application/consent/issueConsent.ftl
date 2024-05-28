<#include '../../layout/layout.ftl'>
<#import '../_applicationContext.ftl' as applicationContextInfo>

<@defaultPage
  pageHeading=pageTitle
  htmlTitle=pageTitle
>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext />

  <@fdsWarning.warning>
    <#if previousConsentApplicationReference?has_content>
      The previous consent ${previousConsentApplicationReference} will be superseded by the revised consent.
      <br><br>
    </#if>
    A notification of the consent will be sent to the applicant and any field equity partners (for field based applications). The
    application status will be set to consented and no further actions can be taken. This action cannot be undone.
  </@fdsWarning.warning>

  <@fdsForm.htmlForm>
    <@fdsAction.submitButtons
      primaryButtonText="Issue consent"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
