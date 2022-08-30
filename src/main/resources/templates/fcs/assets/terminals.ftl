<#include '../layout/layout.ftl'>

<#assign pageTitle = "Terminal: ${terminalName}" />

<@defaultPage
  htmlTitle=pageTitle
  pageHeading=pageTitle
  pageSize=PageSize.TWO_THIRDS_COLUMN
>

  <p class="govuk-body">
    This page will allow you to work with the terminal in question.
  </p>

</@defaultPage>