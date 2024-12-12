package uk.co.nstauthority.fieldconsents.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTests;
import uk.co.nstauthority.fieldconsents.authorisation.SecurityRule;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.fieldconsents",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class ArchitectureTest {

  // disabled while the new annotations are a work in progress
//  @ArchTest
  final ArchTests securityRules = ArchTests.in(SecurityRule.class);
}
