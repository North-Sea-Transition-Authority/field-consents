package uk.co.nstauthority.fieldconsents.authorisation.role.grouped;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationOrRegulatorRole;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
// should match RoleGroup.INDUSTRY_PAY_AND_SUBMIT_APPLICATION_ROLES
@HasApplicationOrRegulatorRole(industryRoles = {Role.SUBMITTER, Role.FINANCE_ADMINISTRATOR})
public @interface UserCanPayAndSubmitApplication {
}
