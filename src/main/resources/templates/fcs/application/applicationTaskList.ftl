<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="taskListSections" type="java.util.List<uk.co.nstauthority.fieldconsents.tasklist.TaskListSection>" -->

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
  notificationBannerContent=deleteBanner
>
    <@taskList.standardTaskList taskListSections=taskListSections/>
</@defaultPage>