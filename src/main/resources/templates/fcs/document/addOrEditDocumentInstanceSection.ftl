<#include '../layout/layout.ftl'>
<#include 'richTextEditor.ftl'>

<#import '_mailMergeFieldSummaryDetails.ftl' as _mailMergeFieldSummaryDetails>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsTextInput.textInput
      path="form.title"
      labelText="Section title"
      hintText="This will be shown in the document sidebar"
    />

    <@richTextEditor path="form.content" labelText="Section content"/>

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
