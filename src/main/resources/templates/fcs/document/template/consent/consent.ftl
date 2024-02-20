<html>
  <head>
    <style>
      @page {
        size: a4;

        @top-center {
          content: element(watermark-ref);
        }
      }

      watermark {
        position: running(watermark-ref);
        z-index: -999;
        color: #b3b3ff;
        font-size: 100px;
        padding-top: 115mm;
        text-align: center;
        font-weight: bold;
        line-height: 90px;
      }
    </style>
  </head>
  <body>
    <#if previewWatermark?has_content>
      <watermark>PREVIEW DOCUMENT</watermark>
    </#if>
    <#list documentInstanceSectionSummaryViews as documentInstanceSectionSummaryView>
      <#assign nestingLevel = documentInstanceSectionSummaryView.nestingLevel()>
      <#assign sectionNumber = documentInstanceSectionSummaryView.sectionNumber()!>
      <#assign content = documentInstanceSectionSummaryView.content()!>

      <#if documentInstanceSectionSummaryView.hasPageBreakBefore()>
        <div style="page-break-before: always;"></div>
      </#if>

      <table style="padding-left: ${nestingLevel}rem; padding-bottom: 0.75rem;">
        <tbody>
          <tr>
            <td style="padding-left: ${nestingLevel+1}rem; padding-right: 1rem; vertical-align: top;">${sectionNumber!}</td>
            <td style="vertical-align: top; white-space: pre-line;">${content!?no_esc}</td>
          </tr>
        </tbody>
      </table>
    </#list>
  </body>
</html>
