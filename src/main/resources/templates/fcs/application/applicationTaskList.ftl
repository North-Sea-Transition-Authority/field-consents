<#include '../layout/layout.ftl'>
<#import './_applicationContext.ftl' as applicationContextInfo>

<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.fieldconsents.tasklist.TaskListSection>" -->
<#-- @ftlvariable name="applicationContext" type="java.util.List<uk.co.nstauthority.fieldconsents.application.ApplicationContextJson>" -->

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
  caption=applicationContext.getPrimaryOperatorName()!""
  notificationBannerContent=deleteBanner
>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContext/>
  <@taskList.standardTaskList taskListSections=taskListSections/>
</@defaultPage>