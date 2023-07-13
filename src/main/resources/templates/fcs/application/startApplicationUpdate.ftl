<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="customerBranding" type="uk.co.nstauthority.fieldconsents.branding.CustomerConfigurationProperties" -->

<#assign pageTitle = "Update application" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=applicationReference
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsStartPage.startPage
    startActionUrl=startActionUrl
    startActionText="Start update">
      <p class="govuk-body">
        Update your application with the information requested by ${customerBranding.mnemonic()}. This may involve correcting details of the application or providing updated supporting documentation.
      </p>
      <@fdsDetails.summaryDetails summaryTitle="What information have I been asked to update?">
        <p class="govuk-body">
          TODO
        </p>
      </@fdsDetails.summaryDetails>
    </@fdsStartPage.startPage>
</@defaultPage>
