<#include '../layout/layout.ftl'>
<#import '_withdrawalRequestSummary.ftl' as withdrawalRequestSummary>

<#assign pageTitle = "Respond to withdrawal request"/>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
backLinkUrl=springUrl(backLinkUrl)>

  <@withdrawalRequestSummary.withdrawalRequestSummary withdrawalRequest=withdrawalRequestView/>
  <@fdsForm.htmlForm actionUrl=springUrl(submitUrl)>
    <@fdsRadio.radioGroup
      path="form.responseStatus"
      hiddenContent=true
      labelText="What is your response to the withdrawal request?">
      <#assign isFirstItem = true/>
      <#list responseStatuses as option, displayText>
        <@fdsRadio.radioItem path="form.responseStatus" itemMap={option: displayText} isFirstItem=isFirstItem>
          <#if option == "REJECTED">
            <@fdsTextarea.textarea
              path="form.responseText.inputValue"
              nestingPath="form.responseStatus"
              labelText="Enter the reason for rejecting the withdrawal request"/>
          </#if>
        </@fdsRadio.radioItem>
      </#list>
    </@fdsRadio.radioGroup>
    <@fdsAction.submitButtons
      primaryButtonText="Submit response"
      secondaryLinkText="Cancel"
      linkSecondaryAction=true
      linkSecondaryActionUrl=springUrl(backLinkUrl)/>
  </@fdsForm.htmlForm>
</@defaultPage>
