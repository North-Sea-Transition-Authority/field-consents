<#include '../layout/layout.ftl'>
<#import './_applicationContext.ftl' as applicationContextInfo>

<@defaultPage
  htmlTitle="Pay and submit application ${applicationReference}"
  pageHeading="${applicationReference}"
  errorItems=errorList
>
  <@applicationContextInfo.applicationContextInfo applicationContext=applicationContextJson />

  <h2 class="govuk-heading-l">Pay and submit application</h2>

  <@fdsInsetText.insetText>
    Please note that by starting the payment, any other person currently completing a payment for the application will have their attempt cancelled.
  </@fdsInsetText.insetText>

  <@fdsDetails.summaryDetails summaryTitle="Share this page for someone else to pay">
    <p>The person who pays for this application must have permission to pay and submit this application.</p>
    <p>You can <@fdsAction.link linkText="send them an email with a link to pay" linkUrl=sharePaymentMailToLink linkClass=linkclass/> or copy this website address and send it to them: <@fdsAction.link linkText=absoluteGetStartPaymentUrl linkUrl=absoluteGetStartPaymentUrl linkClass=linkclass /></p>
  </@fdsDetails.summaryDetails>

  <table class="govuk-table">
    <caption class="govuk-table__caption govuk-table__caption-m">${paymentDescription}</caption>
    <thead class="govuk-table__head">
      <tr class="govuk-table__row">
        <th scope="col" class="govuk-table__header">Item</th>
        <th scope="col" class="govuk-table__header govuk-table__header--numeric">Cost</th>
      </tr>
    </thead>
    <tbody class="govuk-table__body">
      <tr class="govuk-table__row">
        <td class="govuk-table__cell">Charge for submitting an initial application</td>
        <td class="govuk-table__cell govuk-table__cell--numeric">${formattedPaymentAmount}</td>
      </tr>
      <tr class="govuk-table__row">
        <th scope="row" class="govuk-table__header">Total charge</th>
        <th class="govuk-table__cell govuk-table__cell--numeric">${formattedPaymentAmount}</th>
      </tr>
    </tbody>
  </table>

  <div style="display: flex; flex-direction: row">
    <@fdsForm.htmlForm actionUrl=springUrl(startPaymentUrl)>
      <@fdsAction.button buttonText="Start payment" buttonClass="govuk-button govuk-!-margin-right-2" />
    </@fdsForm.htmlForm>

    <#if canReturnToInProgress>
      <@fdsForm.htmlForm actionUrl=springUrl(returnToInProgressUrl)>
        <@fdsAction.button buttonText="Edit application" buttonClass="govuk-button govuk-button--secondary" />
      </@fdsForm.htmlForm>
    </#if>
  </div>

</@defaultPage>
