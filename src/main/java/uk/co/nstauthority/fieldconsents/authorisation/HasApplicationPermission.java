package uk.co.nstauthority.fieldconsents.authorisation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.teams.permissionmanagement.RolePermission;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Security
public @interface HasApplicationPermission {
  RolePermission[] permissions() default {};
}