<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="serviceConfigurationProperties" type="uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="accessibilityConfigurationProperties" type="uk.co.nstauthority.fieldconsents.configuration.AccessibilityConfigurationProperties" -->

<#assign pageTitle = "Contact"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <div class="govuk-body">
    <div>
      <h2 class="govuk-heading-m">Online guidance</h2>
      <ul class="govuk-list">
        <li><a href="${accessibilityConfigurationProperties.productionGuidanceUrl()}" target="_blank">Production applications</a></li>
        <li><a href="${accessibilityConfigurationProperties.flareAndVentGuidanceUrl()}" target="_blank">Flare and vent applications</a></li>
      </ul>
    </div>
    <div>
      <h2 class="govuk-heading-m">Business support</h2>
      <p>For example, questions about filling in your application, the information you need to provide or to provide feedback on the service</p>
      <p>${serviceConfigurationProperties.businessSupportContact().name()}</p>
      <ul class="govuk-list govuk-list--bullet">
        <li>Email:
          <@mailTo.mailToLink
            linkText=serviceConfigurationProperties.businessSupportContact().email()
            mailToEmailAddress=serviceConfigurationProperties.businessSupportContact().email()
          />
        </li>
      </ul>
    </div>
    <div>
      <h2 class="govuk-heading-m">Technical support</h2>
      <p>For example, unexpected problems using the service or system errors being received</p>
      <p>${serviceConfigurationProperties.technicalSupportContact().name()}</p>
      <ul class="govuk-list govuk-list--bullet">
        <li>Telephone: ${serviceConfigurationProperties.technicalSupportContact().phone()}</li>
        <li>Email: <@mailTo.mailToLink
            linkText=serviceConfigurationProperties.technicalSupportContact().email()
            mailToEmailAddress=serviceConfigurationProperties.technicalSupportContact().email()
            />
        </li>
      </ul>
    </div>
  </div>
</@defaultPage>
