package uk.co.nstauthority.fieldconsents.application.duplication;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.data.repository.CrudRepository;

class DuplicationRule {

  @ArchTest
  final ArchRule duplicationAnnotationRule = classes()
      .that().areAssignableTo(CrudRepository.class)
      .should().beAssignableTo(DuplicationSource.class)
      .orShould().beAnnotatedWith(NotDuplicationSource.class);
}
