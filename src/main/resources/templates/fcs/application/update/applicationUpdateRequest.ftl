<#include '../../layout/layout.ftl'>
<#import '../../functions/_getPageSize.ftl' as getPageSize>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>
<#import '../../application/consultation/further-information/furtherInformation.ftl' as furtherInformation>


<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->
<#-- @ftlvariable name="wideSummaryDisplay" type="java.lang.Boolean" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="accordionId" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=applicationReference
  pageSize=getPageSize.getPageSize(wideSummaryDisplay)
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <@fdsDetails.summaryDetails summaryTitle="View application">
    <@applicationSummary.applicationSummary accordionId=accordionId/>
  </@fdsDetails.summaryDetails>
  <#if furtherInformationView?has_content>
    <@furtherInformation.requestDetailsCard furtherInformationView=furtherInformationView/>
  </#if>
  <@grid.gridRow>
    <@grid.twoThirdsColumn>
      <@fdsForm.htmlForm>
        <@fdsTextarea.textarea
          path="form.requestText.inputValue"
          labelText="Application update request details"
          hintText="Explain the changes you would like to the application"/>
        <@fdsDatePicker.datePicker
          path="form.deadlineDate"
          labelText="Deadline date"/>
        <@fdsTimeInput.timeInput
          labelText="Deadline time"
          formId="application-update-deadline-time"
          hoursPath="form.deadlineHours"
          minutesPath="form.deadlineMinutes"/>
        <@fdsAction.submitButtons
          primaryButtonText="Send request"
          secondaryLinkText="Cancel"
          linkSecondaryAction=true
          linkSecondaryActionUrl=springUrl(backLinkUrl)/>
      </@fdsForm.htmlForm>
    </@grid.twoThirdsColumn>
  </@grid.gridRow>
</@defaultPage>
