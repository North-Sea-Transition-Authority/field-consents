package uk.co.nstauthority.fieldconsents.authorisation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.junit.jupiter.params.ParameterizedTest;

@Retention(RetentionPolicy.RUNTIME)
@ParameterizedTest
public @interface ParameterizedSecurityTest {
}
