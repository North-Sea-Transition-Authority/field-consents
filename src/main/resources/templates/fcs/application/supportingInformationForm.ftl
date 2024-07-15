<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Supporting information"/>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
errorItems=errorList
pageSize=PageSize.TWO_THIRDS_COLUMN>
  <@fdsForm.htmlForm>
    <@fdsTextarea.textarea
      path="form.notes.inputValue"
      labelText="Notes"
      hintText="Please add additional information to support the application in the box provided below. There is also an option to attach files to the application towards the bottom of this page if more detailed supporting information is required."
    />
    <#if erapInformationAllowed>
      <@fdsTextarea.textarea
        path="form.erapNotes.inputValue"
        labelText="ERAP alignment studies and projects"
        hintText="Please provide an outline of ERAP activities related to ${applicationType} completed in the year and will be completed for the consent year.
                  Also provide explanation if the requested consent figures are not aligned with the emissions profiles in the asset ERAP/UKSS forecast."/>
    </#if>
    <@fdsFieldset.fieldset
      legendHeading="Supporting documents"
      legendHeadingClass="govuk-heading-m"
      hintText="Upload documents which are appropriate to support your application."
      optionalLabel=true>
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
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
