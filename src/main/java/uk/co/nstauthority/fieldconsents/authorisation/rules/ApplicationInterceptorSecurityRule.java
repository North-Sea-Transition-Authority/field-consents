package uk.co.nstauthority.fieldconsents.authorisation.rules;

import java.lang.annotation.Annotation;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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