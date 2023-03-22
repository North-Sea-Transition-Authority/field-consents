<#include '../layout/layout.ftl'>

<#assign pageTitle = "Supporting information"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle errorItems=errorList pageSize=PageSize.TWO_THIRDS_COLUMN>

  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <@fdsTextarea.textarea
    path="form.notes.inputValue"
    labelText="${form.notes.displayName}"
    hintText="Please add additional information to support the application in the box provided below."
    />
    <#if erapInformationAllowed>
      <@fdsTextarea.textarea
      path="form.erapNotes.inputValue"
      labelText="${form.erapNotes.displayName}"
      hintText="Please provide an outline of ERAP activities related to ${applicationType} completed in the year and will be completed for the consent year.
                Also provide explanation if the requested consent figures are not aligned with the emissions profiles in the asset ERAP/UKSS forecast."
      />
    </#if>
    <@fdsAction.submitButtons
    primaryButtonText="Save and continue"
    secondaryLinkText="Cancel"
    linkSecondaryAction=true
    linkSecondaryActionUrl=springUrl(cancelUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>