<#-- @ftlvariable name="previewWatermark" type="boolean" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->
<#-- @ftlvariable name="documentInstanceSectionsSummaryView" type="uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView" -->

<html>
<head>
  <link rel="stylesheet" href="classpath:///document-assets/all.css"/>
</head>
<body>
  <table class="header">
    <tbody>
      <tr>
        <td style="font-size: 10pt;">Application ref: ${applicationReference}</td>
        <td>
          <img src="classpath:///document-assets/nsta-logo-landscape-black.png" alt="" style="max-height: 20px; float: right;"/>
        </td>
      </tr>
    </tbody>
  </table>
  <#if previewWatermark>
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
  <#list documentInstanceSectionsSummaryView.topLevelDocumentInstanceSectionSummaryViews() as documentInstanceSectionSummaryView>
    <@sectionContentTable documentInstanceSectionSummaryView=documentInstanceSectionSummaryView/>
  </#list>
</body>
</html>

<#macro sectionContentTable documentInstanceSectionSummaryView>
  <#assign sectionNumber = documentInstanceSectionSummaryView.sectionNumber()!>
  <#assign hasPageBreakBefore = documentInstanceSectionSummaryView.hasPageBreakBefore()>
  <#assign content = documentInstanceSectionSummaryView.content()!>
  <#assign children = documentInstanceSectionSummaryView.children()>

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
          <@sectionContentTable documentInstanceSectionSummaryView=child/>
        </#list>
      </td>
    </tr>
    </tbody>
  </table>
</#macro>
