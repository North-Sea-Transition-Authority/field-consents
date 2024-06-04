<#include '../../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Edit consent documents"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  errorItems=errorList>
  <@fdsForm.htmlForm>
    <div class="govuk-!-margin-bottom-6">
      <@fdsResultList.resultList resultCount=documentInstanceSummaryViews?size resultCountSuffix="consent document">
        <#list documentInstanceSummaryViews as documentInstanceSummaryView>
          <@fdsResultList.resultListItem
            linkHeadingUrl=springUrl(documentInstanceSummaryView.viewUrl())
            linkHeadingText=documentInstanceSummaryView.title()
            captionHeadingText=documentInstanceSummaryView.description()/>
        </#list>
      </@fdsResultList.resultList>
    </div>
    <@fdsFieldset.fieldset
      legendHeading="Supporting consent documents"
      legendHeadingClass="govuk-fieldset__legend--s"
      hintText="Upload documents which are appropriate to support the consent."
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
    <@fdsDetails.summaryDetails summaryTitle="What documents can I provide?">
      Documents which support this application. For example, letters from the Secretary of State.
    </@fdsDetails.summaryDetails>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
