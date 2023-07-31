<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign path="form.haveSubmittedEiaDirection"/>
<#assign heading="Have you submitted an EIA screening direction to the Secretary of State or OPRED?">
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
                <@fdsSearchSelector.searchSelectorRest
                path="form.satId"
                nestingPath=path
                restUrl=springUrl(petsSearchRestUrl)
                labelText="EIA screening direction reference"
                preselectedItems={prefilledEiaDirectionRef.id(): prefilledEiaDirectionRef.text()}/>
            </@fdsRadio.radioYes>
            <@fdsRadio.radioNo path=path/>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons
        primaryButtonText="Save and continue"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>
