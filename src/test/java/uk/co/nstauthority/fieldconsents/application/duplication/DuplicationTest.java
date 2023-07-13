package uk.co.nstauthority.fieldconsents.application.duplication;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.junit.ArchTests;

@AnalyzeClasses(
    packages = "uk.co.nstauthority.fieldconsents",
    importOptions = ImportOption.DoNotIncludeTests.class
)
class DuplicationTest {

  @ArchTest
  final ArchTests duplicationRules = ArchTests.in(DuplicationRule.class);
}
