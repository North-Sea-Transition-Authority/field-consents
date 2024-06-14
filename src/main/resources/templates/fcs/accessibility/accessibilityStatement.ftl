<#include '../layout/layout.ftl'>

<#-- @ftlvariable name="accessibilityConfigurationProperties" type="uk.co.nstauthority.fieldconsents.configuration.AccessibilityConfigurationProperties" -->
<#-- @ftlvariable name="serviceConfigurationProperties" type="uk.co.nstauthority.fieldconsents.configuration.ServiceConfigurationProperties" -->
<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->
<#-- @ftlvariable name="serviceBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.ServiceBrandingConfigurationProperties" -->

<#assign pageTitle = "Accessibility statement"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
  <h2 class="govuk-heading-m" id="using-website">Using this website</h2>
  <p class="govuk-body">
    This website is run by the ${customerBrandingConfigurationProperties.name()}
    (${customerBrandingConfigurationProperties.mnemonic()}).
    We want as many people as possible to be able to use this website.
  </p>
  <p class="govuk-body">
    We have also made the website text as simple as possible to understand.
  </p>
  <p class="govuk-body">
    <@fdsAction.link linkText="AbilityNet" linkUrl="https://mcmw.abilitynet.org.uk/" openInNewTab=true/>
    has advice on making your device easier to use if you have a disability.
  </p>

  <h2 class="govuk-heading-m" id="accessibility-coverage">How accessible is this website</h2>
  <p class="govuk-body">
    We know some parts of this website are not fully accessible. We've listed the issues we know about
    in the <@fdsAction.link linkText="non-accessible content" linkUrl="#non-accessible-content" openInNewTab=false/>
    section.
  </p>
  <p class="govuk-body">
    For users of voice dictation software you may have to use in built features in order to input information.
  </p>
  <h2 class="govuk-heading-m" id="reporting-accessibility-problem">Reporting accessibility problems with this website</h2>
  <p class="govuk-body">
    We are always looking to improve the accessibility of this website. If you need information on this website in a
    different format like accessible PDF, large print, easy read, audio recording or braille or if you find any problems
    that are not listed on this page or think we are not meeting the requirements of the accessibility regulations,
    contact:
  </p>

  <p class="govuk-body">${serviceConfigurationProperties.technicalSupportContact().name()}</p>
  <ul class="govuk-list govuk-list--bullet">
    <li>Telephone: ${serviceConfigurationProperties.technicalSupportContact().phone()}</li>
    <li>Email:
      <@mailTo.mailToLink
        linkText=serviceConfigurationProperties.technicalSupportContact().email()
        mailToEmailAddress=serviceConfigurationProperties.technicalSupportContact().email()
        subjectText="Accessibility issue"
      />
    </li>
  </ul>

  <p class="govuk-body">
    We will consider your request and get back to you in 5 working days.
  </p>

  <h2 class="govuk-heading-m" id="technical-info">Technical information about this website’s accessibility</h2>
  <p class="govuk-body">
    The ${customerBrandingConfigurationProperties.name()} is committed to making this website accessible,
    in accordance with the Public Sector Bodies (Websites and Mobile Applications) (No.2) Accessibility Regulations 2018.
  </p>

  <h2 class="govuk-heading-m" id="compliance-status">Compliance status</h2>
  <p class="govuk-body">
    This website is partially compliant with the
      <@fdsAction.link
      linkText="Web Content Accessibility Guidelines version 2.1"
      linkUrl="https://www.w3.org/TR/WCAG21/"
      openInNewTab=true
      />
    AA standard, due to the non-compliances listed below.
  </p>

  <h2 class="govuk-heading-m" id="non-accessible-content">Non-accessible content</h2>
  <p class="govuk-body">The content listed below is non-accessible for the following reasons.</p>

  <h3 class="govuk-heading-s">Non-compliance with the accessibility regulations</h3>
  <ul class="govuk-list govuk-list--bullet">
    <li>
      users are not always notified when conditionally revealed content associated with a radio button or checkbox is
      expanded or collapsed - this fails WCAG 2.1 success criterion 4.1.3 (Status Messages)
    </li>
    <li>
      breadcrumb navigation links are not identified by ARIA landmarks - this fails WCAG 2.1 success criterion
      1.3.1 (Info and Relationships)
    </li>
    <li>
      when uploading a file on macOS with VoiceOver enabled, the user is unable to select the ‘Choose a file’ via
      keyboard only - this fails WCAG 2.1 success criterion 2.1.1 (Keyboard)
    </li>
  </ul>

  <h2 class="govuk-heading-m" id="preparation-statement">Preparation of this accessibility statement</h2>
  <p class="govuk-body">
    This statement was prepared on ${accessibilityConfigurationProperties.statementPreparedDate()}. It was last reviewed
    on ${accessibilityConfigurationProperties.statementLastReviewDate()}.
  </p>
  <p class="govuk-body">
    This website was last tested on ${accessibilityConfigurationProperties.serviceLastTestDate()}. The test was carried out
    by ${accessibilityConfigurationProperties.serviceLastTestedBy()}.
  </p>
  <p class="govuk-body">
    The ${serviceBrandingConfigurationProperties.name()} service
    has been developed using the Energy Portal Design System. The Design System was last
    accessibility tested on ${accessibilityConfigurationProperties.designSystemLastTestDate()}. All features
    on ${serviceBrandingConfigurationProperties.name()} were accessibility tested using automated tools as part of
    the quality assurance process. A manual accessibility review was conducted every 3 weeks on new features developed
    over that period.
  </p>
</@defaultPage>
