<html>
  <body>
    <h1>Production Consent</h1>

    <#list documentSectionSummaryViews as documentSectionSummaryView>
      <h2 id="${documentSectionSummaryView.title()}">
        ${documentSectionSummaryView.title()}
      </h2>

      <p>${documentSectionSummaryView.content()!}</p>
    </#list>
  </body>
</html>
