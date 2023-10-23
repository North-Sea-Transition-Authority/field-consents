<#include '../../../layout/layout.ftl'>
<#import '../../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../further-information/furtherInformation.ftl' as furtherInformation>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="furtherInformationView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.consultation.furtherinformation.FurtherInformationView" -->

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
  <#if furtherInformationView?has_content>
    <@furtherInformation.requestDetailsCard furtherInformationView=furtherInformationView/>
  </#if>
  <@fdsForm.htmlForm>
    <@fdsTextarea.textarea
      path="form.responseText"
      labelText="Further information response"/>
    <@fdsAction.submitButtons
      primaryButtonText="Send response"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
