package uk.co.nstauthority.fieldconsents.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.teams.Role;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Security
public @interface HasAssetOrRegulatorRole {

  Role[] industryRoles() default {};

  Role[] regulatorRoles() default {};

}
