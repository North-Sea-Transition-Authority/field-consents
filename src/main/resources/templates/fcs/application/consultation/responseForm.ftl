<#include '../../layout/layout.ftl'>
<#import '../../summary/_applicationSummary.ftl' as applicationSummary>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=applicationReference
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>
  <#if consultationSummaryView?has_content>
    <@fdsSummaryList.summaryListCard summaryListId="consultation-request-summary" headingText="Consultation details">
      <@fdsSummaryList.summaryListRowNoAction keyText="Deadline">
        ${consultationSummaryView.deadline()}
      </@fdsSummaryList.summaryListRowNoAction>
    </@fdsSummaryList.summaryListCard>
  </#if>

  <@fdsDetails.summaryDetails summaryTitle="View application">
      <@applicationSummary.applicationSummary accordionId=accordionId/>
  </@fdsDetails.summaryDetails>

  <@fdsForm.htmlForm>
    <@fdsRadio.radioGroup
      path="form.habitatsRegsResponseType"
      labelText="What is your response to the NSTA's grant of consent under the Habitats regulations?"
      hiddenContent=true>
      <#list habitatsRegsRadioOptions as radioOption>
        <#assign key = radioOption.name() />
        <#assign value = radioOption.getDisplayName() />
        <@fdsRadio.radioItem path="form.habitatsRegsResponseType" itemMap={key: value}>
          <@fdsTextarea.textarea
            path=radioOption.getTextAreaInputName()
            nestingPath="form.habitatsRegsResponseType"
            labelText=radioOption.getTextAreaDisplayText()
            hintText=radioOption.getTextAreaHintText(applicationReference)/>
        </@fdsRadio.radioItem>
      </#list>
    </@fdsRadio.radioGroup>

    <#if eiaRegsRadioOptions?has_content>
      <@fdsRadio.radioGroup
        path="form.eiaRegsResponseType"
        labelText="What is your response to the NSTA's grant of consent under the EIA regulations?"
        hiddenContent=true>
        <#list eiaRegsRadioOptions as radioOption>
          <#assign key = radioOption.name() />
          <#assign value = radioOption.getDisplayName() />
          <@fdsRadio.radioItem path="form.eiaRegsResponseType" itemMap={key: value}>
            <@fdsTextarea.textarea
              path=radioOption.getTextAreaInputName()
              nestingPath="form.eiaRegsResponseType"
              labelText=radioOption.getTextAreaDisplayText()
              hintText=radioOption.getTextAreaHintText(applicationReference)/>
          </@fdsRadio.radioItem>
        </#list>
      </@fdsRadio.radioGroup>
    </#if>

    <@fdsFieldset.fieldset
      legendHeading="Provide a copy of the Secretary of State's decision"
      legendHeadingClass="govuk-fieldset__legend govuk-fieldset__legend--s"
      hintText="This is required if the Secretary of State has agreed or does not agree to the grant of consent.">
      <@fdsFileUpload.fileUpload
        path=fileUploadAttributes.path()
        allowedExtensions=fileUploadAttributes.allowedExtensions()
        uploadUrl=fileUploadAttributes.uploadUrl()
        downloadUrl=fileUploadAttributes.downloadUrl()
        deleteUrl=fileUploadAttributes.deleteUrl()
        existingFiles=fileUploadAttributes.existingFiles()
        maxAllowedSize=fileUploadAttributes.maxAllowedSize()/>
    </@fdsFieldset.fieldset>

    <@fdsAction.submitButtons
      primaryButtonText="Submit response"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
