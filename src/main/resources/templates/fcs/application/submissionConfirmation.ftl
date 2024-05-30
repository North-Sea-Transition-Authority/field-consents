<#include '../layout/layout.ftl'>

<@defaultPage
htmlTitle=pageTitle>

  <@fdsForm.htmlForm>
    <@fdsPanel.panel
      panelTitle=pageTitle
      panelText="Your reference number"
      panelRef=applicationReference/>

    <h2 class="govuk-heading-m">What happens next</h2>

    <p class="govuk-body">
      We've sent your application to the ${customerBranding.name()} (${customerBranding.mnemonic()}).
    </p>
    <p class="govuk-body">
      They will contact you if further information is required prior to issuing the consent.
    </p>

    <p class="govuk-body">
      <@fdsAction.link
        linkClass="govuk-link"
        linkText="What did you think of this service?"
        linkUrl=springUrl(feedbackUrl)/>
      (takes 30 seconds)
    </p>
    <p class="govuk-body">
      <@fdsAction.link
        linkText="Back to work area"
        linkUrl=springUrl(workAreaUrl)/>
    </p>
  </@fdsForm.htmlForm>
</@defaultPage>
