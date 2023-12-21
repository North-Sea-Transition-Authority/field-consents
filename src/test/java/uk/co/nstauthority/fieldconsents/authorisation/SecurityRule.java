package uk.co.nstauthority.fieldconsents.authorisation;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.co.nstauthority.fieldconsents.mvc.error.DefaultErrorController;

public class SecurityRule {

  @ArchTest
  final ArchRule securityAnnotationRule = methods()
      .that().areNotDeclaredIn(DefaultErrorController.class)
      .and().areMetaAnnotatedWith(RequestMapping.class)
      .should()
        // meta annotated as the annotation is included as part of other annotations
        .beMetaAnnotatedWith(Security.class)
      .andShould()
        // prevent method having this annotation directly as it does nothing on its own
        .notBeAnnotatedWith(Security.class)
      .orShould()
        // check for the annotation on the class level
        .beDeclaredInClassesThat()
        .areMetaAnnotatedWith(Security.class)
      .andShould()
        // check the class doesn't have the annotation directly
        .beDeclaredInClassesThat()
        .areNotAnnotatedWith(Security.class);
}
