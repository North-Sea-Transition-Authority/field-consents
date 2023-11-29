<#include '../../layout/layout.ftl'>
<#import '_selectedApplications.ftl' as selectedApplicationsFtl>

<@defaultPage
htmlTitle=pageTitle
pageHeading=pageTitle
backLinkUrl=springUrl(backLinkUrl)>
  <@selectedApplicationsFtl.summaryList
    applicationDataItems=applicationDataItems
    captionHeadingFunction=captionHeadingFunction
    />
</@defaultPage>
