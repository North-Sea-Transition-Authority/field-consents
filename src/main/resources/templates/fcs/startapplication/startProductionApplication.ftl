<#include '../layout/layout.ftl'>

<#assign pageTitle = "Production Application"/>

<@defaultPage htmlTitle=pageTitle pageHeading=pageTitle>
    <@fdsFlash.flash flashTitle="The guidance of this page needs to be reviewed for the new Field Consents service."/>
    <@fdsStartPage.startPage startActionText="Start Production application" startActionUrl=createProductionUrl>
      <p class="govuk-body">
        PETROLEUM ACT 1998 <br>
        ENERGY ACT 1976 <br>
      </p>
      <ul class="govuk-list govuk-list--bullet">
        <li>These application pages allow companies to apply for Production, Flaring and Venting Consents from 2008 onwards under the above legislation.</li>
        <li>Production Consent applications should be completed for each separate field as appropriate requesting minimum and maximum levels. Flaring and Venting Consents may be requesting for either a single field or a grouping of joint fields.</li>
        <li>For guidance on how to complete the pages, please click on the following links:</li>
        <li><@fdsAction.link linkUrl=flareVentLinkUrl linkText="Flare/Vent Guidance" openInNewTab=true/></li>
        <li><@fdsAction.link linkUrl=explorationLinkUrl linkText="Exploration operatorship" openInNewTab=true/></li>
        <li><@fdsAction.link linkUrl=onshoreLinkUrl linkText="Onshore operatorship" openInNewTab=true/></li>
        <li>Any queries relating to the Field Consent forms, or a specific application, should be addressed to the Aberdeen office:</li>
        <li>NSTA Regulation <br>
          2nd Floor <br>
          AB1 Building <br>
          48 Huntly St <br>
          Aberdeen <br>
          AB10 1SH <br></li>
        <li>NSTA Consents & Authorisations <br>
          E-mail: consents@nstauthority.co.uk <br>
          Tel: +44 (0)300 020 1014 or +44 (0)300 020 1044 <br>
        </li>
      </ul>
      <br>
    </@fdsStartPage.startPage>
</@defaultPage>