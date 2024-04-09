<#-- @ftlvariable name="previewWatermark" type="boolean" -->
<#-- @ftlvariable name="applicationReference" type="java.lang.String" -->
<#-- @ftlvariable name="documentInstanceSectionsSummaryView" type="uk.co.fivium.digitaldocumentlibrary.document.DocumentInstanceSectionsSummaryView" -->
<#-- @ftlvariable name="customerBrandingConfigurationProperties" type="uk.co.nstauthority.fieldconsents.branding.CustomerBrandingConfigurationProperties" -->

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
          ${customerBrandingConfigurationProperties.name()} is a business name of the ${customerBrandingConfigurationProperties.legalName()}.
          ${customerBrandingConfigurationProperties.legalName()} is a limited company registered in England and Wales with
          registered number ${customerBrandingConfigurationProperties.registeredNumber()} and VAT registered number
          ${customerBrandingConfigurationProperties.vatNumber()}. Our registered office is at ${customerBrandingConfigurationProperties.address()}.
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
