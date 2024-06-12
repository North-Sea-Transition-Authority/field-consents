<#include '../layout/layout.ftl'>

<#assign pageTitle = "Add case note"/>

<@defaultPage
  htmlTitle=pageTitle
  backLinkUrl=springUrl(backLinkUrl)
  errorItems=errorList>

  <@fdsForm.htmlForm>
    <@fdsTextarea.textarea
      path="form.caseNoteText.inputValue"
      labelText="Case note"
      caption=captionTitle
      pageHeading=true
      labelHeadingClass="govuk-label--xl"/>
    <@fdsFieldset.fieldset
      legendHeading="Case note documents"
      legendHeadingClass="govuk-heading-m"
      hintText="Upload documents for this case note."
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
      primaryButtonText="Add note"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
