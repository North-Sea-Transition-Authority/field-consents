<#include '../layout/layout.ftl'>

<#import '_mailMergeFieldSummaryDetails.ftl' as _mailMergeFieldSummaryDetails>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsTextInput.textInput
      path="form.title"
      labelText="Title"
      hintText="This will be shown in the document sidebar"
    />

    <@fdsTextarea.textarea path="form.content" labelText="Text" rows = "12" />

    <#if mailMergeFieldViews?has_content>
      <@_mailMergeFieldSummaryDetails.mailMergeFieldSummaryDetails mailMergeFieldViews />
    </#if>

    <@fdsAction.submitButtons
      primaryButtonText="${submitButtonText}"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
