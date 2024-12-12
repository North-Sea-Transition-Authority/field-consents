package uk.co.nstauthority.fieldconsents.authorisation.role.grouped;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.authorisation.HasApplicationOrRegulatorRole;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@HasApplicationOrRegulatorRole(
    regulatorRoles = {
        Role.CASE_OFFICER,
        Role.CASE_MANAGER,
        Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
        Role.TECHNICAL_REVIEWER,
        Role.VIEWER,
    },
    consulteeRoles = {
        Role.ALLOCATOR,
        Role.RESPONDER,
    },
    industryRoles = {
        Role.CREATOR,
        Role.EDITOR,
        Role.SUBMITTER,
        Role.FINANCE_ADMINISTRATOR,
        Role.VIEWER,
    }
)
public @interface UserCanViewApplication {
}
