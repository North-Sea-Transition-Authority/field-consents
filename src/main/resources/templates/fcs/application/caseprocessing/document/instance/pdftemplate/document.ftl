<#-- @ftlvariable name="previewWatermark" type="boolean" -->
<#-- @ftlvariable name="documentInstanceSectionsSummaryView" type="uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView" -->

<html>
<head>
  <link rel="stylesheet" href="classpath:///document-assets/all.css"/>
</head>
<body>
  <table class="header">
    <tbody>
      <tr>
        <td style="font-size: 10pt;">Application ref: APPLICATION_REF</td>
        <td>
          <img src="classpath:///document-assets/nsta-logo-landscape-black.png" alt="" style="max-height: 20px; float: right;"/>
        </td>
      </tr>
    </tbody>
  </table>
  <#if previewWatermark?has_content>
    <div class="watermark">
      PREVIEW DOCUMENT
    </div>
  </#if>
  <table class="footer">
    <tbody>
      <tr>
        <td class="page-number"></td>
        <td>
          North Sea Transition Authority is a business name of the Oil and Gas Authority. Oil and Gas Authority is a limited company registered in England and
          Wales with registered number 09666504 and VAT registered number 249433979. Our registered office is at Sanctuary Buildings, 20 Great Smith Street,
          London, SW1P 3BT.
        </td>
      </tr>
    </tbody>
  </table>
  <#list documentInstanceSectionsSummaryView.sectionSummaryViews() as sectionSummaryView>
    <@sectionContentTable documentInstanceSectionsSummaryView=sectionSummaryView/>
  </#list>
</body>
</html>

<#macro sectionContentTable documentInstanceSectionsSummaryView>
  <#assign nestingLevel = documentInstanceSectionsSummaryView.nestingLevel()>
  <#assign sectionNumber = documentInstanceSectionsSummaryView.sectionNumber()!>
  <#assign hasPageBreakBefore = documentInstanceSectionsSummaryView.hasPageBreakBefore()>
  <#assign content = documentInstanceSectionsSummaryView.content()!>
  <#assign children = documentInstanceSectionsSummaryView.children()>

  <#if hasPageBreakBefore>
    <div style="page-break-after: always;"></div>
  </#if>

  <table>
    <tbody>
    <tr>
      <td style="vertical-align: top;">
        <#if sectionNumber?has_content>
          ${sectionNumber}
        </#if>
      </td>
      <td style="vertical-align: top;">
        ${content?no_esc}
      </td>
    </tr>
    <tr>
      <td></td>
      <td>
        <#list children as child>
          <@sectionContentTable documentInstanceSectionsSummaryView=child/>
        </#list>
      </td>
    </tr>
    </tbody>
  </table>
</#macro>
