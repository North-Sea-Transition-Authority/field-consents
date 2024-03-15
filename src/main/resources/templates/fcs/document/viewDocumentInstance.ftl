<#include '../layout/layout.ftl'>

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

<@defaultPageWithSubNavigation htmlTitle=pageTitle>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentInstanceSectionsSummaryView.sectionSummaryViews() as sectionSummaryView>
          <@fdsSubNavigation.subNavigationNestedLink
            linkText=sectionSummaryView.titleWithSectionNumber()
            linkUrl="#${sectionSummaryView.titleWithSectionNumber()}"
          />
        </#list>
      </@fdsSubNavigation.subNavigationSection>
    </@fdsSubNavigation.subNavigation>
  </@defaultPageWithSubNavigationSubNav>

  <@defaultPageWithSubNavigationContent pageHeading=pageTitle>
    <#if documentInstanceSectionsSummaryView.errorMessages()?has_content>
      <@errorSummary errors=documentInstanceSectionsSummaryView.errorMessages()/>
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

    <#list documentInstanceSectionsSummaryView.sectionSummaryViews() as sectionSummaryView>
      <div>
        <h2 id="${sectionSummaryView.titleWithSectionNumber()}" class="govuk-heading-l govuk-!-margin-bottom-2">
          ${sectionSummaryView.titleWithSectionNumber()}
        </h2>
        <#if sectionSummaryView.hasPageBreakBefore()>
          <strong class="govuk-tag govuk-tag--blue govuk-body govuk-secondary-text-colour govuk-!-font-weight-bold">NEW PAGE</strong>
        </#if>
      </div>

      <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
        <@fdsActionDropdown.actionDropdownItem
          actionText="Add section before"
          linkAction=true
          linkActionUrl=springUrl(sectionSummaryView.documentInstanceSectionUrls().addSectionBeforeUrl())
          linkActionScreenReaderText=sectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Add section after"
          linkAction=true
          linkActionUrl=springUrl(sectionSummaryView.documentInstanceSectionUrls().addSectionAfterUrl())
          linkActionScreenReaderText=sectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Add subsection"
          linkAction=true
          linkActionUrl=springUrl(sectionSummaryView.documentInstanceSectionUrls().addSubsectionUrl())
          linkActionScreenReaderText=sectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Edit"
          linkAction=true
          linkActionUrl=springUrl(sectionSummaryView.documentInstanceSectionUrls().editUrl())
          linkActionScreenReaderText=sectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Remove"
          linkAction=true
          linkActionUrl=springUrl(sectionSummaryView.documentInstanceSectionUrls().removeUrl())
          linkActionScreenReaderText=sectionSummaryView.titleWithSectionNumber()
        />
      </@fdsActionDropdown.actionDropdown>

      <div class="govuk-body govuk-!-margin-top-4">
        ${(sectionSummaryView.content()!)?no_esc}
      </div>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
