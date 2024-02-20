<#include '../layout/layout.ftl'>

<#import '_mailMergeFieldSummaryDetails.ftl' as _mailMergeFieldSummaryDetails>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsTextInput.textInput
      path="form.title"
      labelText="Title"
      hintText="This will be shown in the document sidebar"
    />

    <#if conditionsFdsSelectMap?has_content>
      <@fdsSelect.select
        path="form.conditionMnemonic"
        options=conditionsFdsSelectMap
        labelText="Select a condition"
        hintText="This section will only be included in a document if the condition is met"
        optionalInputDefault="Select one..."
        optionalLabel=true
      />
    </#if>

    <@fdsTextarea.textarea path="form.content" labelText="Text" rows = "12" />

    <@fdsRadio.radioGroup path="form.numbered" labelText="Should this section be numbered?">
      <@fdsRadio.radioYes path="form.numbered" />
      <@fdsRadio.radioNo path="form.numbered" />
    </@fdsRadio.radioGroup>

    <@fdsRadio.radioGroup path="form.hasPageBreakBefore" labelText="Should this section start on a new page?">
      <@fdsRadio.radioYes path="form.hasPageBreakBefore" />
      <@fdsRadio.radioNo path="form.hasPageBreakBefore" />
    </@fdsRadio.radioGroup>

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
