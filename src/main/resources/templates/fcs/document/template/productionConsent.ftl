<html>
  <body>
    <h1>Production Consent</h1>

    <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
      <h2 id="${documentInstanceSectionSummaryView.title()}">
        ${documentInstanceSectionSummaryView.title()}
      </h2>

      <p>${documentInstanceSectionSummaryView.content()!}</p>
    </#list>
  </body>
</html>
