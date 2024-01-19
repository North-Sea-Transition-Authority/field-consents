<#macro defaultDocument>
  <html>
  <body>
  <#nested/>
  </body>
  </html>
</#macro>

<#macro sections documentInstanceSectionSummaryViews>
  <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
    <#assign nestingLevel = documentInstanceSectionSummaryView.nestingLevel()>
    <#assign sectionNumber = documentInstanceSectionSummaryView.sectionNumber()!>
    <#assign content = documentInstanceSectionSummaryView.content()!>
    <table style="padding-left: ${nestingLevel}rem; padding-bottom: 0.75rem;">
      <tbody>
      <tr>
        <td style="padding-left: ${nestingLevel+1}rem; padding-right: 1rem; vertical-align: top;">${sectionNumber!}</td>
        <td style="vertical-align: top; white-space: pre-line;">${content!}</td>
      </tr>
      </tbody>
    </table>
  </#list>
</#macro>
