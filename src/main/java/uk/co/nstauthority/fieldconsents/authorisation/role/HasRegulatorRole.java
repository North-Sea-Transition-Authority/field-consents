package uk.co.nstauthority.fieldconsents.authorisation.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.authorisation.Security;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Security
public @interface HasRegulatorRole {
  Role value();
}