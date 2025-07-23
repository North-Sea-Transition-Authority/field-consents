<#include '../../../layout/layout.ftl'>
<#import '../../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../consultation.ftl' as consultation>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
<#-- @ftlvariable name="consultationRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.ConsultationRequestView" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  caption=applicationReference
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsDetails.summaryDetails summaryTitle="View application">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
  </@fdsDetails.summaryDetails>
  <#if consultationRequestView?has_content>
    <@consultation.requestDetailsCard consultationRequestView=consultationRequestView/>
  </#if>
  <@fdsForm.htmlForm>
    <@fdsTextarea.textarea
      path="form.requestText"
      labelText="What further information would you like to request?"
      hintText="Explain what further information you would like, either added to the application or from ${customerBrandingConfigurationProperties.mnemonic()}."/>
    <@fdsAction.submitButtons
      primaryButtonText="Send request"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
