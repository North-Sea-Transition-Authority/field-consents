<#include '../../../../layout/layout.ftl'>

<#--Error Summary/Input Errors Component without jump links-->
<#-- https://design-system.service.gov.uk/components/error-summary/ -->
<#macro errorSummary errors>
  <div class="govuk-error-summary" aria-labelledby="error-summary-title" role="alert" data-module="govuk-error-summary">
    <h2 class="govuk-error-summary__title" id="error-summary-title">There is a problem</h2>
    <div class="govuk-error-summary__body">
      <ul class="govuk-list govuk-error-summary__list">
        <#list errors as error>
          <li class="fcs-error-summary__message">${error}</li>
        </#list>
      </ul>
    </div>
  </div>
</#macro>

<@defaultPageWithSubNavigation htmlTitle=pageTitle backLinkUrl=springUrl(backLinkUrl)>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentInstanceSectionsSummaryView.topLevelDocumentInstanceSectionSummaryViews() as documentInstanceSectionSummaryView>
          <@documentSectionSummaryViewSubnavigationItems documentInstanceSectionSummaryView/>
        </#list>
      </@fdsSubNavigation.subNavigationSection>
    </@fdsSubNavigation.subNavigation>
  </@defaultPageWithSubNavigationSubNav>

  <@defaultPageWithSubNavigationContent pageHeading=pageTitle>
    <#if documentInstanceSectionsSummaryView.allErrorMessages()?has_content>
      <@errorSummary errors=documentInstanceSectionsSummaryView.allErrorMessages()/>
    </#if>

    <@fdsAction.link
      linkText="Preview document"
      linkUrl=springUrl(previewUrl)
      linkClass="govuk-button govuk-button--blue"
      role=true
      openInNewTab=true
    />

    <@fdsAction.link
      linkText="Reload document"
      linkUrl=springUrl(reloadUrl)
      linkClass="govuk-button govuk-button--secondary"
      role=true
    />

    <#list documentInstanceSectionsSummaryView.topLevelDocumentInstanceSectionSummaryViews() as documentInstanceSectionSummaryView>
      <@documentInstanceSectionSummaryViewContent documentInstanceSectionSummaryView/>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>

<#macro documentSectionSummaryViewSubnavigationItems documentInstanceSectionSummaryView>
  <@fdsSubNavigation.subNavigationNestedLink
    linkText=documentInstanceSectionSummaryView.titleWithSectionNumber()
    linkUrl="#${documentInstanceSectionSummaryView.titleWithSectionNumber()}"
  />

  <#list documentInstanceSectionSummaryView.children() as child>
    <@documentSectionSummaryViewSubnavigationItems documentInstanceSectionSummaryView=child/>
  </#list>
</#macro>

<#macro documentInstanceSectionSummaryViewContent documentInstanceSectionSummaryView>
  <div>
    <h2 id="${documentInstanceSectionSummaryView.titleWithSectionNumber()}" class="govuk-heading-l govuk-!-margin-bottom-2">
      ${documentInstanceSectionSummaryView.titleWithSectionNumber()}
    </h2>
    <#if documentInstanceSectionSummaryView.hasPageBreakBefore()>
      <strong class="govuk-tag govuk-tag--blue govuk-body govuk-secondary-text-colour govuk-!-font-weight-bold">NEW PAGE</strong>
    </#if>
  </div>

  <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
    <@fdsActionDropdown.actionDropdownItem
      actionText="Add section before"
      linkAction=true
      linkActionUrl=springUrl(documentInstanceSectionSummaryView.documentInstanceSectionUrls().addSectionBeforeUrl())
      linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
    />

    <@fdsActionDropdown.actionDropdownItem
      actionText="Add section after"
      linkAction=true
      linkActionUrl=springUrl(documentInstanceSectionSummaryView.documentInstanceSectionUrls().addSectionAfterUrl())
      linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
    />

    <@fdsActionDropdown.actionDropdownItem
      actionText="Add subsection"
      linkAction=true
      linkActionUrl=springUrl(documentInstanceSectionSummaryView.documentInstanceSectionUrls().addSubsectionUrl())
      linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
    />

    <@fdsActionDropdown.actionDropdownItem
      actionText="Edit"
      linkAction=true
      linkActionUrl=springUrl(documentInstanceSectionSummaryView.documentInstanceSectionUrls().editUrl())
      linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
    />

    <@fdsActionDropdown.actionDropdownItem
      actionText="Remove"
      linkAction=true
      linkActionUrl=springUrl(documentInstanceSectionSummaryView.documentInstanceSectionUrls().removeUrl())
      linkActionScreenReaderText=documentInstanceSectionSummaryView.titleWithSectionNumber()
    />
  </@fdsActionDropdown.actionDropdown>

  <div class="govuk-body govuk-!-margin-top-4">
    ${(documentInstanceSectionSummaryView.content()!)?no_esc}
  </div>

  <#list documentInstanceSectionSummaryView.children() as child>
    <@documentInstanceSectionSummaryViewContent documentInstanceSectionSummaryView=child/>
  </#list>
</#macro>
