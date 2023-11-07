<#include '../../layout/layout.ftl'>
<#import '../../summary/_summaryDetails.ftl' as summaryDetails>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>

<#-- @ftlvariable name="errorList" type="java.util.List<uk.co.nstauthority.fieldconsents.validation.ErrorItem>" -->
<#-- @ftlvariable name="consultationSummaryItems" type="java.util.List<uk.co.nstauthority.fieldconsents.summary.SummaryItem>" -->

<#assign pageTitle = "Consultations"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=applicationReference
  backLinkUrl=springUrl(backLinkUrl)
  pageSize=PageSize.FULL_WIDTH
  errorItems=errorList>
  <@caseProcessingActions.caseActions actions=actionList/>
  <#if consultationSummaryItems?has_content>
    <#list consultationSummaryItems as summaryItem>
      <h2 class="govuk-heading-l">${summaryItem.displayName()}</h2>
      <@summaryDetails.summaryDetails summaryItem=summaryItem/>
    </#list>
  <#else>
    <@fdsInsetText.insetText>
      No consultations have taken place on this case.
    </@fdsInsetText.insetText>
  </#if>
</@defaultPage>
