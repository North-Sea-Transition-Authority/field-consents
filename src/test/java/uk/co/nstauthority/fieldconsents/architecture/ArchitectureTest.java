package uk.co.nstauthority.fieldconsents.architecture;

import com.tngtech.archunit.core.domain.properties.HasName;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTests;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Test;
import uk.co.fivium.digitalenummaterialisationlibrary.enummaterialisation.MaterialisableEnum;
import uk.co.nstauthority.fieldconsents.FieldConsentsApplication;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRule;
import uk.co.nstauthority.fieldconsents.util.enumutil.Displayable;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.fieldconsents",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

  // disabled while the new annotations are a work in progress
//  @ArchTest
  final ArchTests securityRules = ArchTests.in(SecurityRule.class);

  @Test
  void displayableEnumsAreMaterialised() {
    var javaClasses = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackagesOf(FieldConsentsApplication.class);

    ArchRuleDefinition.classes()
        .that().areEnums()
        .and().containAnyMethodsThat(
            HasName.Predicates.name("getDisplayName").or(HasName.Predicates.name("getDisplayOrder"))
        )
        .should().implement(MaterialisableEnum.class)
        .check(javaClasses);

    ArchRuleDefinition.theClass(Displayable.class)
        .should().beAssignableTo(MaterialisableEnum.class);
  }
}
