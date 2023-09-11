package uk.co.nstauthority.fieldconsents.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;
import uk.co.nstauthority.fieldconsents.authorisation.TestSecurityRules;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.fieldconsents",
    importOptions = ImportOption.OnlyIncludeTests.class
)
class TestArchitectureTest {

  @ArchTest
  final ArchTests securityRules = ArchTests.in(TestSecurityRules.class);
}
