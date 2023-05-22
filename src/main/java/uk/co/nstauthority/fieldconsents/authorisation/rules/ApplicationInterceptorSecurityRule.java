package uk.co.nstauthority.fieldconsents.authorisation.rules;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import uk.co.nstauthority.fieldconsents.application.ApplicationVersion;
import uk.co.nstauthority.fieldconsents.authentication.ServiceUserDetail;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRuleResult;

public interface ApplicationInterceptorSecurityRule {

  Class<? extends Annotation> supports();

  SecurityRuleResult check(Object annotation,
                           HttpServletRequest request,
                           HttpServletResponse response,
                           ServiceUserDetail user,
                           ApplicationVersion applicationVersion);

}
