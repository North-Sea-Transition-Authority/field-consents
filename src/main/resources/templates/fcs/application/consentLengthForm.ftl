<#include '../layout/layout.ftl'>

<#assign pageTitle = "Consent Length"/>

<@defaultPage htmlTitle="pageTitle" pageHeading="" errorItems=errorList>
    <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
        <@fdsRadio.radioGroup path="form.consentLengthType" hiddenContent=true labelText="Select the period of the consent you are applying for" fieldsetHeadingSize="h1" fieldsetHeadingClass="govuk-fieldset__legend--l">
            <@fdsInsetText.insetText>
                The consent length will determine the data you need to provide in the following sections of the Task List
            </@fdsInsetText.insetText>
            <#assign isFirstItem = true/>
            <#list consentTypes as option, displayText>
                <@fdsRadio.radioItem path="form.consentLengthType" itemMap={option: displayText} isFirstItem=isFirstItem>
                    <#if option == "SHORT_TERM">
                        <@fdsDateInput.dateInput
                            labelText="Start date"
                            dayPath="form.shortTermStartDay.inputValue" monthPath="form.shortTermStartMonth.inputValue" yearPath="form.shortTermStartYear.inputValue"
                            formId="form.shortTermStartDate"
                            nestingPath="form.consentLengthType"/>
                        <@fdsDateInput.dateInput
                            labelText="End date"
                            dayPath="form.shortTermEndDay.inputValue" monthPath="form.shortTermEndMonth.inputValue" yearPath="form.shortTermEndYear.inputValue"
                            formId="form.shortTermEndDate"
                            nestingPath="form.consentLengthType"/>
                        <#elseif option == "ANNUAL">
                            <@fdsRadio.radioGroup path="form.annualConsentYear.inputValue" hiddenContent=true labelText="Year" nestingPath="form.consentLengthType">
                                <#list annualConsentYears as annualYear, annualYearText>
                                    <@fdsRadio.radioItem path="form.annualConsentYear.inputValue" itemMap={annualYear: annualYearText}/>
                                </#list>
                            </@fdsRadio.radioGroup>
                        <#else>
                            <@fdsSelect.select path="form.longTermStartYear.inputValue" options=longTermStartYears labelText="Start year" nestingPath="form.consentLengthType"/>
                        <@fdsTextInput.textInput path="form.longTermEndYear.inputValue" labelText="End year" nestingPath="form.consentLengthType" labelClass="govuk-label--s" inputClass="govuk-input--width-5"/>
                    </#if>
                </@fdsRadio.radioItem>
                <#assign isFirstItem = false/>
            </#list>
        </@fdsRadio.radioGroup>
        <@fdsAction.submitButtons linkSecondaryAction=true linkSecondaryActionUrl="${springUrl(cancelUrl)}" primaryButtonText="Save and complete" secondaryLinkText="Cancel"/>
    </@fdsForm.htmlForm>
</@defaultPage>