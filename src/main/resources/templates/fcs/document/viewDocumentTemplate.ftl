<#include '../layout/layout.ftl'>

<@defaultPageWithSubNavigation htmlTitle=pageTitle>
  <@defaultPageWithSubNavigationSubNav smallSubnav=true>
    <@fdsSubNavigation.subNavigation>
      <@fdsSubNavigation.subNavigationSection>
        <#list documentTemplateSectionSummaryViews as documentTemplateSectionSummaryView>
          <@fdsSubNavigation.subNavigationNestedLink
            linkText=documentTemplateSectionSummaryView.titleWithSectionNumber()
            linkUrl="#${documentTemplateSectionSummaryView.titleWithSectionNumber()}"
          />
        </#list>
      </@fdsSubNavigation.subNavigationSection>
    </@fdsSubNavigation.subNavigation>
  </@defaultPageWithSubNavigationSubNav>

  <@defaultPageWithSubNavigationContent pageHeading=pageTitle>
    <#list documentTemplateSectionSummaryViews as documentTemplateSectionSummaryView>
      <div>
        <h2 id="${documentTemplateSectionSummaryView.titleWithSectionNumber()}" class="govuk-heading-l govuk-!-margin-bottom-2">
          ${documentTemplateSectionSummaryView.titleWithSectionNumber()}
        </h2>
        <#if documentTemplateSectionSummaryView.hasPageBreakBefore()>
          <strong class="govuk-tag govuk-tag--blue govuk-body govuk-secondary-text-colour govuk-!-font-weight-bold">NEW PAGE</strong>
        </#if>
      </div>

      <#if documentTemplateSectionSummaryView.conditionTitle()?has_content>
        <div class="govuk-hint">Condition: ${documentTemplateSectionSummaryView.conditionTitle()}</div>
      </#if>

      <@fdsActionDropdown.actionDropdown dropdownButtonText="Section actions">
        <@fdsActionDropdown.actionDropdownItem
          actionText="Add section before"
          linkAction=true
          linkActionUrl=springUrl(documentTemplateSectionSummaryView.documentTemplateSectionUrls().addSectionBeforeUrl())
          linkActionScreenReaderText=documentTemplateSectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Add section after"
          linkAction=true
          linkActionUrl=springUrl(documentTemplateSectionSummaryView.documentTemplateSectionUrls().addSectionAfterUrl())
          linkActionScreenReaderText=documentTemplateSectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Add subsection"
          linkAction=true
          linkActionUrl=springUrl(documentTemplateSectionSummaryView.documentTemplateSectionUrls().addSubsectionUrl())
          linkActionScreenReaderText=documentTemplateSectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Edit"
          linkAction=true
          linkActionUrl=springUrl(documentTemplateSectionSummaryView.documentTemplateSectionUrls().editUrl())
          linkActionScreenReaderText=documentTemplateSectionSummaryView.titleWithSectionNumber()
        />

        <@fdsActionDropdown.actionDropdownItem
          actionText="Remove"
          linkAction=true
          linkActionUrl=springUrl(documentTemplateSectionSummaryView.documentTemplateSectionUrls().removeUrl())
          linkActionScreenReaderText=documentTemplateSectionSummaryView.titleWithSectionNumber()
        />
      </@fdsActionDropdown.actionDropdown>

      <p class="govuk-body govuk-body__preserve-whitespace govuk-!-margin-top-4">${documentTemplateSectionSummaryView.content()!}</p>
    </#list>
  </@defaultPageWithSubNavigationContent>
</@defaultPageWithSubNavigation>
