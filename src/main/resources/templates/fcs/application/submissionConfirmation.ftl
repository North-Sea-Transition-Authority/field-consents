<#include '../layout/layout.ftl'>

<@defaultPage
htmlTitle=pageTitle>

  <@fdsForm.htmlForm>
    <@fdsPanel.panel
    panelTitle=pageTitle
    panelText="Your reference number"
    panelRef=caseReference
   />

   <@fdsAction.link
    linkText="Back to work area"
    linkUrl=springUrl(workAreaUrl)
    />
  </@fdsForm.htmlForm>
</@defaultPage>