<#include '../../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->

<#assign path="form.forPurposeOfEiaRegs"/>
<#assign heading="Is this a \"project\" for the purposes of the Offshore Oil and Gas Exploration, Production, Unloading and Storage (Environmental Impact Assessment) Regulations 2020?">
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
        fieldsetHeadingSize="h1">
            <@fdsRadio.radioYes path=path/>
            <@fdsRadio.radioNo path=path/>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons
        primaryButtonText="Save and continue"
        secondaryLinkText="Cancel"
        linkSecondaryAction=true
        linkSecondaryActionUrl=springUrl(cancelUrl)/>
    </@fdsForm.htmlForm>
</@defaultPage>
