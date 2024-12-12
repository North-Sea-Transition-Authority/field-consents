package uk.co.nstauthority.fieldconsents.teams.management.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import uk.co.nstauthority.fieldconsents.teams.TeamType;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface InvokingUserIsMemberOfStaticTeam {
  TeamType value();
}
