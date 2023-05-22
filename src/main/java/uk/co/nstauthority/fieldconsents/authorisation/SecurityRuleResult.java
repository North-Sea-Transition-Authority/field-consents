package uk.co.nstauthority.fieldconsents.authorisation;

import jakarta.annotation.Nullable;
import org.springframework.http.HttpStatus;

public record SecurityRuleResult(
    boolean hasRulePassed, // controls whether the for-loop keeps going
    @Nullable HttpStatus failureStatus,
    @Nullable String failureMessage,
    @Nullable String redirectUrl // a redirect url may be optionally provided
) {

  /**
   * Indicates that the security rule has passed all its checks and the process flow should continue as normal.
   *
   * @return security rule result that doesn't alter the handler interceptors normal operating behaviour
   */
  public static SecurityRuleResult continueAsNormal() {
    return new SecurityRuleResult(true, null, null, null);
  }

  public static SecurityRuleResult checkFailedWithStatus(HttpStatus httpStatus) {
    return checkFailedWithStatusAndMessage(httpStatus, null);
  }

  public static SecurityRuleResult checkFailedWithStatusAndMessage(HttpStatus httpStatus, String failureMessage) {
    return new SecurityRuleResult(false, httpStatus, failureMessage, null);
  }

  /**
   * Indicates that the handler interceptor should not execute Spring's remaining handler interceptors. This security
   * rule dealt with the response itself and no further Spring intervention is required.
   *
   * @return security rule result that redirects the response and cancels all Spring handler interceptors
   */
  public static SecurityRuleResult cancelRemainingHandlerInterceptorsAndRedirect(String redirectUrl) {
    return new SecurityRuleResult(false, null, null, redirectUrl);
  }
}
