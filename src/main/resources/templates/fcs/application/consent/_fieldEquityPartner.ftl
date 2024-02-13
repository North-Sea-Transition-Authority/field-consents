<#import '../../../fds/components/details/details.ftl' as fdsDetails>
<#import '../../../fds/components/notificationBanner/notificationBanner.ftl' as fdsNotificationBanner>
<#import '../../../fds/components/warning/warning.ftl' as fdsWarning>

<#-- @ftlvariable name="fieldEquityPartnersView" type="uk.co.nstauthority.fieldconsents.application.fieldequitypartner.FieldEquityPartnersView" -->

<#macro notificationBanner fieldEquityPartnersView regulatorIndustryAccessManagerRole industryAccessManagerRole consentRecipientRole>
  <#assign regulatorIndustryAccessManager = regulatorIndustryAccessManagerRole.getDisplayName()?lower_case />
  <#assign industryAccessManager = industryAccessManagerRole.getDisplayName()?lower_case />
  <#assign consentRecipient = consentRecipientRole.getDisplayName()?lower_case />

  <@fdsNotificationBanner.notificationBannerInfo bannerTitleText="Field Equity Partner notice">
    <@fdsNotificationBanner.notificationBannerContent headingText="The following industry teams do not exist or are missing a consent recipient user">
      <ul class="govuk-list govuk-list--bullet">
        <#list fieldEquityPartnersView.organisationGroupNamesWithoutConsentRecipients() as organisationGroupName>
          <li class="govuk-list__item">${organisationGroupName}</li>
        </#list>
      </ul>
      <@fdsDetails.details
        detailsTitle="What if an industry team listed above does not exist?"
        detailsText="A regulator ${regulatorIndustryAccessManager} will need to create a new team for that organisation group and add a user with the ${consentRecipient} role."/>
      <@fdsDetails.details
        detailsTitle="What if an industry team listed above is missing a consent recipient user?"
        detailsText="An ${industryAccessManager} from that industry team or a regulator ${regulatorIndustryAccessManager} must give an applicable user the ${consentRecipient} role."/>
      <@fdsWarning.warning>
        The organisations listed above will not receive receipt that a consent has been issued.
      </@fdsWarning.warning>
    </@fdsNotificationBanner.notificationBannerContent>
  </@fdsNotificationBanner.notificationBannerInfo>
</#macro>

<#macro summaryList fieldEquityPartnersView>
  <@fdsDetails.summaryDetails summaryTitle="Field Equity Partners">
    <ul class="govuk-list govuk-list--bullet">
      <#list fieldEquityPartnersView.fieldEquityPartnerNames() as fieldEquityPartnerName>
        <li class="govuk-list__item">${fieldEquityPartnerName}</li>
      </#list>
    </ul>
  </@fdsDetails.summaryDetails>
</#macro>
