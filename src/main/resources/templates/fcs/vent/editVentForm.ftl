<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList>
  <@fdsForm.htmlForm>
    <@fdsRadio.radio
    labelText="Vent system type"
    path="form.ventType"
    radioItems=ventTypes
    />
    <@fdsTextarea.textarea path="form.description.inputValue" labelText="Description" rows="2"/>
    <@fdsRadio.radioGroup
    path="form.meteredFlag"
    labelText="Metered"
    hiddenContent=true
    >
      <@fdsRadio.radioYes path="form.meteredFlag">
        <@fdsTextarea.textarea
        path="form.commentsMeteredYes.inputValue"
        nestingPath="form.meteredFlag"
        labelText="Comments"
        optionalLabel=true
        rows="2"
        />
      </@fdsRadio.radioYes>
      <@fdsRadio.radioNo path="form.meteredFlag">
        <@fdsTextarea.textarea
        path="form.commentsMeteredNo.inputValue"
        nestingPath="form.meteredFlag"
        labelText="Comments"
        hintText="Explain if there are issues with the meter. For example, the meters are offline"
        rows="2"
        />
      </@fdsRadio.radioNo>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
    primaryButtonText="Save and continue"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>
