<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="expiringLicences" type="java.util.List<uk.co.nstauthority.fieldconsents.licences.LicenceView>" -->
<#-- @ftlvariable name="expiredLicence" type="uk.co.nstauthority.fieldconsents.licences.LicenceView" -->
<#macro expiringLicencesBanner expiringLicences>
  <#if expiringLicences?has_content>
    <#if expiringLicences?size == 1>
      <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Information">
        <@fdsNotificationBanner.notificationBannerContent headingText="The following licence is due to expire before the consent period">
          <#list expiringLicences as expiringLicence>
            ${expiringLicence.licenceRef()} expires on ${expiringLicence.scheduleExpiryDate()}
          </#list>
        </@fdsNotificationBanner.notificationBannerContent>
      </@fdsNotificationBanner.notificationBannerInfo>
    <#else>
      <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Information">
        <@fdsNotificationBanner.notificationBannerContent headingText="The following licences are due to expire before the consent period">
          <ul class="govuk-list govuk-list--bullet">
            <#list expiringLicences as expiringLicence>
              <li class="govuk-list__item">${expiringLicence.licenceRef()} expires on ${expiringLicence.scheduleExpiryDate()}</li>
            </#list>
          </ul>
        </@fdsNotificationBanner.notificationBannerContent>
      </@fdsNotificationBanner.notificationBannerInfo>
    </#if>
  </#if>
</#macro>
