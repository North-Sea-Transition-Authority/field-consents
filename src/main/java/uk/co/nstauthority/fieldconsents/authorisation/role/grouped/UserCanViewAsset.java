package uk.co.nstauthority.fieldconsents.authorisation.role.grouped;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.authorisation.HasAssetOrRegulatorRole;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@HasAssetOrRegulatorRole(
    regulatorRoles = {
        Role.CASE_OFFICER,
        Role.CASE_MANAGER,
        Role.CONSENTS_AND_AUTHORISATIONS_MANAGER,
        Role.TECHNICAL_REVIEWER,
        Role.VIEWER
    },
    industryRoles = {
        Role.CREATOR,
        Role.EDITOR,
        Role.SUBMITTER,
        Role.FINANCE_ADMINISTRATOR,
        Role.VIEWER,
        Role.CONSENT_RECIPIENT
    }
)
public @interface UserCanViewAsset {
}
