<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign path="form.haveEiaDirectionToSubmit"/>
<#assign heading="Do you have an EIA screening direction that still needs to be submitted?"/>
<#assign caption="EIA screening direction"/>

<@defaultPage
htmlTitle=heading
caption=caption
errorItems=errorList
backLinkUrl=springUrl(backLinkUrl)
>
    <@fdsForm.htmlForm>
        <@fdsRadio.radioGroup
        path=path
        labelText=heading
        fieldsetHeadingClass="govuk-fieldset__legend--l"
        fieldsetHeadingSize="h1"
        hiddenContent=true>
            <@fdsRadio.radioYes path=path>
                <@fdsDateInput.dateInput
                labelText="What is the latest date this will be submitted?"
                dayPath="form.latestDateToBeSubmitted.dayInput.inputValue"
                monthPath="form.latestDateToBeSubmitted.monthInput.inputValue"
                yearPath="form.latestDateToBeSubmitted.yearInput.inputValue"
                formId="form.latestDateToBeSubmitted"
                nestingPath=path/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path=path>
                <@fdsTextarea.textarea
                path="form.whyNoEiaDirection.inputValue"
                nestingPath=path
                labelText="Explain why you don’t intend to submit an EIA screening direction"/>
            </@fdsRadio.radioNo>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons
        primaryButtonText="Save and continue"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>
