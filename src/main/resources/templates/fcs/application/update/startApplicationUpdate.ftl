<#include '../../layout/layout.ftl'>
<#import '_applicationUpdateRequestSummary.ftl' as applicationUpdateRequestSummary>

<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.ApplicationUpdateRequestView" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->

<#assign pageTitle = "Update application"/>

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
        Update your application with the information requested by ${customerBrandingConfigurationProperties.mnemonic()}. This may involve correcting details of the application or providing updated supporting documentation.
      </p>
      <@applicationUpdateRequestSummary.applicationUpdateRequestSummary applicationUpdateRequestView=applicationUpdateRequestView/>
    </@fdsStartPage.startPage>
</@defaultPage>
