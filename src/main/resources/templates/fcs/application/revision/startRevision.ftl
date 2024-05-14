<#include '../../layout/layout.ftl'>

<#assign pageTitle = "Revise consent" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=applicationReference
  backLinkUrl=springUrl(backLinkUrl)
>
  <@fdsStartPage.startPage
    startActionUrl=""
    startActionText="Start revision"
  >
    <p class="govuk-body">
      Click start revision to begin a consent revision. After a revision has been consented, any previous consents will be
      superseded by the new consent.
    </p>
  </@fdsStartPage.startPage>
</@defaultPage>
