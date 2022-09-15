<#include '../layout/layout.ftl'>

<#assign pageTitle = "Field: ${fieldName}"/>

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>

    <@fdsStartPage.startPage startActionText="Start an application" startActionUrl=startApplicationUrl>
        <p class="govuk-body">
          This page will allow you to work with the field in question.
        </p>
    </@fdsStartPage.startPage>

</@defaultPage>