<#include '../layout/layout.ftl'>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsTextInput.textInput
      path="form.title"
      labelText="Title"
      hintText="This will be shown in the document sidebar"
    />

    <@fdsSelect.select
      path="form.conditionMnemonic"
      options=conditionsFdsSelectMap
      labelText="Select a condition"
      hintText="This section will only be included in a document if the condition is met"
      optionalInputDefault="Select one..."
      optionalLabel=true
    />

    <@fdsTextarea.textarea path="form.content" labelText="Text" rows = "12" />

    <@fdsAction.submitButtons
      primaryButtonText="${submitButtonText}"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
