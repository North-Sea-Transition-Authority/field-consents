<#include '../layout/layout.ftl'>
<#include '../application/review/reviewDetails.ftl'/>
<#import '../functions/_getPageSize.ftl' as getPageSize>
<#import '../summary/_applicationSummary.ftl' as applicationSummary>

<#-- @ftlvariable name="pageTitle" type="java.lang.String" -->
<#-- @ftlvariable name="technicalReviewDeadline" type="java.lang.String" -->
<#-- @ftlvariable name="technicalReviewNotes" type="java.lang.String" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->
<#-- @ftlvariable name="wideSummaryDisplay" type="java.lang.Boolean" -->
<#-- @ftlvariable name="backLinkUrl" type="java.lang.String" -->
<#-- @ftlvariable name="accordionId" type="java.lang.String" -->
<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign pageTitle = "Submit technical review"/>

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
    <@reviewDetails technicalReviewSummaryView=technicalReviewSummaryView/>
    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup
        path="form.responseType"
        labelText="Decision"
        fieldsetHeadingSize="h2"
        fieldsetHeadingClass="govuk-fieldset__legend--m"
        hiddenContent=true>
            <@fdsRadio.radioItem
            path="form.responseType"
            itemMap={approveRadio.toString(): approveRadio.getDisplayName()}>
                <@fdsTextarea.textarea
                path="form.consentConditions.inputValue"
                nestingPath="form.responseType"
                labelText=approveRadio.getResponseTextLabel()
                hintText=approveRadio.getResponseTextLabelHint()
                optionalLabel=true/>
            </@fdsRadio.radioItem>
            <@fdsRadio.radioItem path="form.responseType" itemMap={rejectRadio.toString(): rejectRadio.getDisplayName()}>
                <@fdsTextarea.textarea
                path="form.rejectionReason.inputValue"
                nestingPath="form.responseType"
                labelText=rejectRadio.getResponseTextLabel()
                hintText=rejectRadio.getResponseTextLabelHint()/>
            </@fdsRadio.radioItem>
        </@fdsRadio.radioGroup>
        <@fdsFieldset.fieldset
        legendHeading="Upload documents which support your response"
        legendHeadingClass="govuk-fieldset__legend--m"
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
        primaryButtonText="Submit technical review"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(backLinkUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>
