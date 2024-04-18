<#include '../layout/layout.ftl'>
<#import './_applicationContext.ftl' as applicationContextInfo>
<#import 'update/_applicationUpdateRequestHiddenSummary.ftl' as applicationUpdateRequestHiddenSummary>

<#-- @ftlvariable name="successfulDeleteBanner" type="String" -->
<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.fieldconsents.tasklist.TaskListSection>" -->
<#-- @ftlvariable name="applicationContext" type="java.util.List<uk.co.nstauthority.fieldconsents.application.ApplicationContext>" -->
<#-- @ftlvariable name="applicationUpdateRequestView" type="uk.co.nstauthority.fieldconsents.application.caseprocessing.update.request.ApplicationUpdateRequestView" -->

<#if successfulDeleteBanner?has_content>
  <#assign deleteBanner>
    <@fdsNotificationBanner.notificationBannerSuccess bannerTitleText="Success">
      <@fdsNotificationBanner.notificationBannerContent>
        ${successfulDeleteBanner}
      </@fdsNotificationBanner.notificationBannerContent>
    </@fdsNotificationBanner.notificationBannerSuccess>
  </#assign>
</#if>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  caption=applicationReference!""
  notificationBannerContentOverride=deleteBanner>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext/>
  <#if warning?has_content>
    <@fdsWarning.warning>${warning}</@fdsWarning.warning>
  </#if>
  <@applicationUpdateRequestHiddenSummary.applicationUpdateRequestHiddenSummary applicationUpdateRequestView=applicationUpdateRequestView!""/>
  <@fdsAction.link
    linkText="Delete application"
    linkUrl=springUrl(deleteApplicationUrl)
    linkClass="govuk-button govuk-button--secondary"/>
  <@taskList.standardTaskList taskListSections=taskListSections/>
</@defaultPage>
