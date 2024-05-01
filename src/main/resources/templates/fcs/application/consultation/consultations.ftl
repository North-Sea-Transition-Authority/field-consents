<#include '../../layout/layout.ftl'>
<#import '../../summary/_summaryDetails.ftl' as summaryDetails>
<#import '../_caseProcessingActions.ftl' as caseProcessingActions>
<#import './consultation.ftl' as consultation>

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
  <@consultation.consultationList consultationSummaryItems=consultationSummaryItems/>
</@defaultPage>
