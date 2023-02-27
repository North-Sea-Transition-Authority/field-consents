<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<@defaultPage htmlTitle="EIA screening direction" errorItems=errorList>
  <@fdsForm.htmlForm>
    <#assign haveSubmittedEiaDirection="form.haveSubmittedEiaDirection">
    <@fdsRadio.radioGroup
      path=haveSubmittedEiaDirection
      labelText="Have you submitted an EIA screening direction?"
      fieldsetHeadingSize="h1"
      fieldsetHeadingClass="govuk-fieldset__legend--xl"
      hiddenContent=true>
        <@fdsRadio.radioYes path=haveSubmittedEiaDirection>
          <@fdsSearchSelector.searchSelectorRest
            path="form.satId"
            nestingPath=haveSubmittedEiaDirection
            restUrl=springUrl("/data-sources/eia-directions")
            labelText="EIA screening direction reference"
            preselectedItems={prefilledEiaDirectionRef.id(): prefilledEiaDirectionRef.text()}/>
        </@fdsRadio.radioYes>
        <@fdsRadio.radioNo path=haveSubmittedEiaDirection>
          <#assign haveEiaDirectionToSubmit="form.haveEiaDirectionToSubmit">
          <@fdsRadio.radioGroup
            path=haveEiaDirectionToSubmit
            nestingPath=haveSubmittedEiaDirection
            labelText="Do you have an EIA screening direction that still needs to be submitted?"
            hiddenContent=true>
            <@fdsRadio.radioYes path=haveEiaDirectionToSubmit>
              <@fdsDateInput.dateInput
              labelText="What is the latest date this will be submitted?"
              dayPath="form.latestDateToBeSubmitted.dayInput.inputValue"
              monthPath="form.latestDateToBeSubmitted.monthInput.inputValue"
              yearPath="form.latestDateToBeSubmitted.yearInput.inputValue"
              formId="form.latestDateToBeSubmitted"
              nestingPath=haveEiaDirectionToSubmit/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path=haveEiaDirectionToSubmit>
              <@fdsTextarea.textarea
                path="form.whyNoEiaDirection.inputValue"
                nestingPath=haveEiaDirectionToSubmit
                labelText="Explain why you don’t intend to submit an EIA screening direction"/>
            </@fdsRadio.radioNo>
          </@fdsRadio.radioGroup>
        </@fdsRadio.radioNo>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
      primaryButtonText="Save and continue"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(cancelUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>