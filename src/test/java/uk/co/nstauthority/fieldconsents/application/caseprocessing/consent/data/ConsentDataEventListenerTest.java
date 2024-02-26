package uk.co.nstauthority.fieldconsents.application.caseprocessing.consent.data;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import uk.co.nstauthority.fieldconsents.application.consentlength.ConsentLengthChangeEvent;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.fieldconsents.application.caseprocessing.consent",
    importOptions = ImportOption.DoNotIncludeTests.class
)
public class ConsentDataEventListenerTest {

  @ArchTest
  final ArchRule onConsentLengthChangeEvent_isTransactionalAfterCommit = ArchRuleDefinition.methods()
      .that()
        .areDeclaredIn(ConsentDataService.class)
        .and()
        .haveRawParameterTypes(ConsentLengthChangeEvent.class)
        .should(haveTransactionalEventListenerWithPhase(TransactionPhase.BEFORE_COMMIT));

  private static ArchCondition<JavaMethod> haveTransactionalEventListenerWithPhase(TransactionPhase transactionPhase) {
    return new ArchCondition<>("have annotation TransactionalEventListener with phase %s".formatted(transactionPhase)) {
      @Override
      public void check(JavaMethod item, ConditionEvents events) {
        try {
          if (transactionPhase != item.getAnnotationOfType(TransactionalEventListener.class).phase()) {
            var ruleViolationMessage = String.format(
                "Method %s is annotated with @TransactionalEventListener but phase is not set to %s",
                item.getFullName(),
                transactionPhase
            );
            events.add(SimpleConditionEvent.violated(item, ruleViolationMessage));
          }
        }
        catch (IllegalArgumentException exception) {
          var ruleViolationMessage = String.format("Method %s is not annotated with @TransactionalEventListener", item.getFullName());
          events.add(SimpleConditionEvent.violated(item, ruleViolationMessage));
        }
      }
    };
  }

}
