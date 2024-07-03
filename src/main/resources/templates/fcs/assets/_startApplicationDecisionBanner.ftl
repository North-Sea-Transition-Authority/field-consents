<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="errorList" type="uk.co.nstauthority.fieldconsents.assets.StartApplicationDecision" -->

<#macro banner startApplicationDecision>
  <#if !startApplicationDecision.canBeStarted()>
    <@grid.gridRow>
      <@grid.twoThirdsColumn>
        <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Missing information">
          <@fdsNotificationBanner.notificationBannerContent headingText="Applications cannot be started because">
            <ul>
              <#list startApplicationDecision.reasonsWhyCannotBeStarted() as reason>
                <li>${reason}</li>
              </#list>
            </ul>
            Contact <@mailTo.mailToLink mailToEmailAddress=customerBrandingConfigurationProperties.email() /> if you think there has been a mistake.
          </@fdsNotificationBanner.notificationBannerContent>
        </@fdsNotificationBanner.notificationBannerInfo>
      </@grid.twoThirdsColumn>
    </@grid.gridRow>
  </#if>
</#macro>
